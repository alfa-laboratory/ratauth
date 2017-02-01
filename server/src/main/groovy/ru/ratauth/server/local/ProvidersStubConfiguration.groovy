package ru.ratauth.server.local

import groovy.transform.CompileStatic
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import ru.ratauth.exception.AuthorizationException
import ru.ratauth.exception.RegistrationException
import ru.ratauth.providers.assurance.dto.AssuranceStatus
import ru.ratauth.providers.auth.AuthProvider
import ru.ratauth.providers.auth.dto.AuthInput
import ru.ratauth.providers.auth.dto.AuthResult
import ru.ratauth.providers.auth.dto.BaseAuthFields
import ru.ratauth.providers.registrations.RegistrationProvider
import ru.ratauth.providers.registrations.dto.AssuredRegResult
import ru.ratauth.providers.registrations.dto.RegInput
import ru.ratauth.providers.registrations.dto.RegResult
import rx.Observable
/**
 * @author mgorelikov
 * @since 03/11/15
 */
@CompileStatic
class ProvidersStubConfiguration {
  public static final String REG_CREDENTIAL = 'credential'
  public static final String REG_CODE = '123'

  abstract class AbstractAuthProvider implements AuthProvider, RegistrationProvider {}

  @Bean(name = 'STUB')
  @Primary
  AbstractAuthProvider authProvider() {
    return new AbstractAuthProvider() {
      @Override
      Observable<AuthResult> authenticate(AuthInput input) {
        if (input.data.get(BaseAuthFields.USERNAME.val()) == 'login' && input.data.get(BaseAuthFields.PASSWORD.val()) == 'password')
          return Observable.just(AuthResult.builder().userId('userId').userInfo([(BaseAuthFields.USER_ID.val()): 'user_id'] as Map).build())
        else
          return Observable.error(new AuthorizationException(AuthorizationException.ID.CREDENTIALS_WRONG))
      }

      @Override
      Observable<Boolean> checkUserStatus(AuthInput input) {
        return Observable.just(true)
      }

      @Override
      int getProvidedAssuranceLevel() {
        return 1
      }

      @Override
      Observable<RegResult> register(RegInput input) {
        if (!input.data.containsKey(BaseAuthFields.CODE.val())) { //first step of registration
          //one step registration
          if (input.data.get(BaseAuthFields.USERNAME.val()) == 'login' && input.data.get(BaseAuthFields.PASSWORD.val()) == 'password') {
            return Observable.just(RegResult.builder().userId('userId').userInfo([(BaseAuthFields.USER_ID.val()): 'user_id'] as Map)
                .status(RegResult.Status.SUCCESS).build())
          } else if (input.data.get(REG_CREDENTIAL) == 'credential') {//two step registration
            return Observable.<RegResult> just(AssuredRegResult.builder().userInfo([
                (BaseAuthFields.USERNAME.val()): 'login',
                (BaseAuthFields.CODE.val())    : 'code'] as Map)
                .assuranceLevel('1')
                .assuranceStatus(AssuranceStatus.ACTIVATED)
                .assuranceData(['id'      : 'someExternalId',
                                'someFlag': true] as Map)
                .status(RegResult.Status.NEED_APPROVAL).build())
          } else {
            return Observable.error(new RegistrationException("Registration failed"))
          }
        }
      }
    }
  }
}

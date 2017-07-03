package ru.ratauth.server.local

import groovy.transform.CompileStatic
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import ru.ratauth.exception.AuthorizationException
import ru.ratauth.exception.RegistrationException
import ru.ratauth.providers.auth.Activator
import ru.ratauth.providers.auth.AuthProvider
import ru.ratauth.providers.auth.Verifier
import ru.ratauth.providers.auth.dto.ActivateInput
import ru.ratauth.providers.auth.dto.ActivateResult
import ru.ratauth.providers.auth.dto.AuthInput
import ru.ratauth.providers.auth.dto.AuthResult
import ru.ratauth.providers.auth.dto.BaseAuthFields
import ru.ratauth.providers.auth.dto.VerifyInput
import ru.ratauth.providers.auth.dto.VerifyResult

import ru.ratauth.providers.registrations.RegistrationProvider
import ru.ratauth.providers.registrations.dto.RegInput
import ru.ratauth.providers.registrations.dto.RegResult
import rx.Observable

import static ru.ratauth.providers.auth.dto.VerifyResult.Status.NEED_APPROVAL
import static ru.ratauth.providers.auth.dto.VerifyResult.Status.SUCCESS

/**
 * @author mgorelikov
 * @since 03/11/15
 */
@CompileStatic
class ProvidersStubConfiguration {
  public static final String REG_CREDENTIAL = 'credential'
  public static final String REG_CODE = '123'

  abstract class AbstractAuthProvider implements AuthProvider, RegistrationProvider {}

  abstract class AbstractProvider implements Activator, Verifier {}

  @Bean(name = 'STUBIdentityProvider')
  @Primary
  Activator activator() {
    return new AbstractProvider() {
      @Override
      Observable<ActivateResult> activate(ActivateInput input) {
        return Observable.just(new ActivateResult().with {
          if(input.data.username) {
            it.data.put("user_id", input.data.username.reverse())
          }
          return it
        })
      }

      @Override
      Observable<VerifyResult> verify(VerifyInput input) {
        if(input.data.password == "password") {
          return Observable.just(new VerifyResult(status: SUCCESS).with {
            if(input.data.username) {
              it.data.put("user_id", input.data.username.reverse())
            }
            return it
          })
        } else return Observable.error(new AuthorizationException(AuthorizationException.ID.CREDENTIALS_WRONG))
      }
    }
  }

  @Bean(name = 'STUB')
  @Primary
  AbstractAuthProvider authProvider() {
    return new AbstractAuthProvider() {
      @Override
      Observable<AuthResult> authenticate(AuthInput input) {
        if (input.data.get(BaseAuthFields.USERNAME.val()) == 'login' && input.data.get(BaseAuthFields.PASSWORD.val()) == 'password')
          return Observable.just(AuthResult.builder()
                  .data([(BaseAuthFields.USER_ID.val()): 'user_id'] as Map)
                  .authContext(["credentials"] as Set)
                  .status(AuthResult.Status.SUCCESS).build())
        else
          return Observable.error(new AuthorizationException(AuthorizationException.ID.CREDENTIALS_WRONG))
      }

      @Override
      boolean isAuthCodeSupported() {
        return false
      }

      @Override
      Observable<Boolean> checkUserStatus(AuthInput input) {
        return Observable.just(true)
      }

      @Override
      Observable<RegResult> register(RegInput input) {
        if (!input.data.containsKey(BaseAuthFields.CODE.val())) { //first step of registration
          //one step registration
          if (input.data.get(BaseAuthFields.USERNAME.val()) == 'login' && input.data.get(BaseAuthFields.PASSWORD.val()) == 'password')
            return Observable.just(RegResult.builder().authContext(["credentials"] as Set).data([(BaseAuthFields.USER_ID.val()): 'user_id'] as Map)
              .status(RegResult.Status.SUCCESS).build())
          else if (input.data.get(REG_CREDENTIAL) == 'credential') //two step registration
            return Observable.just(RegResult.builder().data([
                (BaseAuthFields.USERNAME.val()): 'login',
                (BaseAuthFields.CODE.val()): 'code'] as Map)
              .status(RegResult.Status.NEED_APPROVAL).build())
          else
            return Observable.error(new RegistrationException("Registration failed"))
        } else {//second step of registration
          if (input.data.get(BaseAuthFields.CODE.val()) == REG_CODE && input.data.get(BaseAuthFields.USERNAME.val()) == 'login')
            return Observable.just(RegResult.builder().redirectUrl('http://relying.party/gateway')
            // TODO@ruslan когда приходит код, это место должен обрабатывать сам сервер авторизации
              .authContext(["code"] as Set)
              .data([(BaseAuthFields.USER_ID.val()): 'user_id'] as Map)
              .status(RegResult.Status.SUCCESS).build())
          else
            return Observable.error(new RegistrationException("Registration failed"))
        }
      }

      @Override
      boolean isRegCodeSupported() {
        return true
      }
    }
  }
}

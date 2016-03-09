package ru.ratauth.server.configuration

import groovy.transform.CompileStatic
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import ratpack.spring.config.EnableRatpack
import ru.ratauth.entities.*
import ru.ratauth.exception.AuthorizationException
import ru.ratauth.exception.ExpiredException
import ru.ratauth.exception.RegistrationException
import ru.ratauth.providers.auth.AuthProvider
import ru.ratauth.providers.auth.dto.AuthInput
import ru.ratauth.providers.auth.dto.AuthResult
import ru.ratauth.providers.auth.dto.BaseAuthFields
import ru.ratauth.providers.registrations.RegistrationProvider
import ru.ratauth.providers.registrations.dto.RegInput
import ru.ratauth.providers.registrations.dto.RegResult
import ru.ratauth.server.utils.DateUtils
import ru.ratauth.services.ClientService
import ru.ratauth.services.SessionService
import ru.ratauth.services.TokenCacheService
import rx.Observable

import java.time.LocalDateTime

/**
 * @author mgorelikov
 * @since 03/11/15
 */
@EnableRatpack
@Configuration
@SpringBootApplication
@CompileStatic
class ProvidersStubConfiguration {
  abstract class AbstractAuthProvider implements AuthProvider, RegistrationProvider {}

  @Bean(name = 'STUB')
  @Primary
  public AbstractAuthProvider authProvider() {
    return new AbstractAuthProvider() {
      @Override
      Observable<AuthResult> authenticate(AuthInput input) {
        if (input.data.get(BaseAuthFields.USERNAME.val()) == 'login' && input.data.get(BaseAuthFields.PASSWORD.val()) == 'password')
          return Observable.just(AuthResult.builder().data([(BaseAuthFields.USER_ID.val()): 'user_id'] as Map).status(AuthResult.Status.SUCCESS).build())
        else
          return Observable.error(new AuthorizationException("Authorization failed"));
      }

      @Override
      boolean isAuthCodeSupported() {
        return false
      }

      @Override
      Observable<RegResult> register(RegInput input) {
        if (input.data.get(BaseAuthFields.USERNAME.val()) == 'login' && input.data.get(BaseAuthFields.PASSWORD.val()) == 'password')
          return Observable.just(RegResult.builder().data([(BaseAuthFields.USER_ID.val()): 'user_id'] as Map).status(RegResult.Status.SUCCESS).build())
        else
          return Observable.error(new RegistrationException("Registration failed"));
      }

      @Override
      boolean isRegCodeSupported() {
        return false
      }
    }
  }
}

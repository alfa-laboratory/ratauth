package ru.ratauth.server.configuration

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ratpack.spring.config.EnableRatpack
import ru.ratauth.entities.AuthCodeStatus
import ru.ratauth.entities.AuthzEntry
import ru.ratauth.entities.RelyingParty
import ru.ratauth.entities.Token
import ru.ratauth.providers.AuthProvider
import ru.ratauth.services.AuthzEntryService
import ru.ratauth.services.RelyingPartyService
import ru.ratauth.services.TokenService
import rx.Observable

/**
 * @author mgorelikov
 * @since 03/11/15
 */
@EnableRatpack
@Configuration
@SpringBootApplication
class ProvidersConfiguration {

  public static final String TOKEN = 'sometoken'
  public static final String TOKEN_ID = 'sometoken_id'


  @Bean
  public RelyingPartyService relyingPartyService(@Value('${auth.secret.code}') String secret) {
    return new RelyingPartyService() {
      @Override
      Observable<RelyingParty> getRelyingParty(String id) {
        return Observable.just(RelyingParty.builder()
            .redirectURL('token?response_type=token&username=login&password=password')
            .id('id')
            .identityProvider('BANK')
            .secret(secret)
            .password('secret')
            .resourceServer('stub')
            .resourceServer('stub2')
            .baseAddress('http://ratauth.ru')
            .build())
      }
    }
  }

  @Bean
  public AuthzEntryService authCodeService() {
    return new AuthzEntryService() {
      @Override
      Observable<AuthzEntry> save(AuthzEntry code) {
        return Observable.just(code)
      }

      @Override
      Observable<AuthzEntry> get(String code) {
        if (code == '1234')
          return Observable.just(new AuthzEntry(code: code,
              relyingParty: 'sense',
              identityProvider: 'BANK',
              scopes: ['read'],
              resourceServers: ['stub'],
              status: AuthCodeStatus.NEW))
      }
    }
  }

  @Bean
  public TokenService tokenService() {
    return new TokenService() {
      @Override
      Observable<Token> save(Token token) {
        return Observable.just(token)
      }

      @Override
      Observable<Token> get(String token) {
        if (token == TOKEN)
          return Observable.just(new Token(token: TOKEN,
              TTL: 36000l,
              created: new Date(),
              tokenId: TOKEN_ID,
              scopes: ['read'],
              resourceServers: ['stub']))
      }
    }
  }

  @Bean(name = 'BANK')
  public AuthProvider authProvider() {
    return new AuthProvider() {
      @Override
      Observable<Map<String, String>> checkCredentials(String login, String password) {
        if (login == 'login' && password == 'password')
          return Observable.just([(AuthProvider.USER_ID): 'user_id'] as Map)
      }
    }
  }
}

package ru.ratauth.server.configuration

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ratpack.spring.config.EnableRatpack
import ru.ratauth.entities.AuthzEntry
import ru.ratauth.entities.RelyingParty
import ru.ratauth.entities.Token
import ru.ratauth.providers.AuthProvider
import ru.ratauth.services.AuthzEntryService
import ru.ratauth.services.RelyingPartyService
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
      Observable<AuthzEntry> getByValidCode(String code, Date now) {
        if (code == '1234')
          return Observable.just(new AuthzEntry(authCode: code,
            relyingParty: 'sense',
            identityProvider: 'BANK',
            scopes: ['read'],
            resourceServers: ['stub'],
            baseJWT: 'eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiJzZW5zZSIsImlfdXNlcl9pZCI6InVzZXJfaWQiLCJpc3MiOiJodHRwOlwvXC9yYXRhdXRoLnJ1IiwiZXhwIjoxNDQ3MjcwMDYzLCJpYXQiOjE0NDcyNjk5NzYsInJwX2Jhc2VfYWRkcmVzcyI6WyJodHRwOlwvXC9yYXRhdXRoLnJ1Il0sImp0aSI6ImJkNjM2OTI4LTcxOTYtMzlhNy04OWY2LTc4Zjk0Njc2NTRlYiJ9.YP-bMI6QQ7OBrjHWqQUAIKcG_ME7Ipbbtqp8To_oyf0'
          ))
      }


      @Override
      Observable<AuthzEntry> getByValidRefreshToken(String token, Date now) {
        if (token == '1234')
          return Observable.just(new AuthzEntry(authCode: 'code',
            relyingParty: 'sense',
            identityProvider: 'BANK',
            scopes: ['read'],
            resourceServers: ['stub'],
            refreshToken: '1234',
            baseJWT: 'eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiJzZW5zZSIsImlfdXNlcl9pZCI6InVzZXJfaWQiLCJpc3MiOiJodHRwOlwvXC9yYXRhdXRoLnJ1IiwiZXhwIjoxNDQ3MjcwMDYzLCJpYXQiOjE0NDcyNjk5NzYsInJwX2Jhc2VfYWRkcmVzcyI6WyJodHRwOlwvXC9yYXRhdXRoLnJ1Il0sImp0aSI6ImJkNjM2OTI4LTcxOTYtMzlhNy04OWY2LTc4Zjk0Njc2NTRlYiJ9.YP-bMI6QQ7OBrjHWqQUAIKcG_ME7Ipbbtqp8To_oyf0',
            tokens: [new Token(token: TOKEN,
              TTL: 36000l,
              created: new Date(),
              idToken: TOKEN_ID)]
          ))
      }

      @Override
      Observable<AuthzEntry> getByValidToken(String token, Date now) {
        if (token == TOKEN)
          return Observable.just(new AuthzEntry(authCode: 'code',
            relyingParty: 'sense',
            identityProvider: 'BANK',
            scopes: ['read'],
            resourceServers: ['stub'],
            tokens: [new Token(token: TOKEN,
              TTL: 36000l,
              created: new Date(),
              idToken: TOKEN_ID)]
          ))
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

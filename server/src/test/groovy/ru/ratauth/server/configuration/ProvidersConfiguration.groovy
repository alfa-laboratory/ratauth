package ru.ratauth.server.configuration

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ratpack.spring.config.EnableRatpack
import ru.ratauth.entities.AuthCode
import ru.ratauth.entities.RelyingParty
import ru.ratauth.entities.Token
import ru.ratauth.providers.AuthProvider
import ru.ratauth.services.AuthCodeService
import ru.ratauth.services.RelyingPartyService
import ru.ratauth.services.TokenService

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
  public RelyingPartyService relyingPartyService(@Value('${auth.secret}') String secret) {
    return new RelyingPartyService() {
      @Override
      RelyingParty getRelyingParty(String id) {
        return RelyingParty.builder()
          .redirectURL('token?response_type=token&username=login&password=password')
          .id('id')
          .identityProvider('BANK')
          .secret(secret)
          .password('secret')
          .build()
      }
    }
  }

  @Bean
  public AuthCodeService authCodeService() {
    return new AuthCodeService() {
      @Override
      AuthCode save(AuthCode code) {
        return code
      }

      @Override
      AuthCode get(String code) {
        if(code == '1234')
          return new AuthCode(code: code,
            relyingParty: 'sense',
            identityProvider: 'BANK',
            scopes: ['read'])
      }
    }
  }

  @Bean
  public TokenService tokenService() {
    return new TokenService() {
      @Override
      Token save(Token token) {
        return token
      }

      @Override
      Token get(String token) {
        if(token == TOKEN)
          return new Token(token: TOKEN,
            TTL: 36000l,
            created: new Date(),
          tokenId: TOKEN_ID,
          scopes: ['read'])
      }
    }
  }

  @Bean(name = 'BANK')
  public AuthProvider authProvider() {
    return new AuthProvider() {
      @Override
      Map<String, String> checkCredentials(String login, String password) {
        if(login =='login' && password == 'password')
        return [(AuthProvider.USER_ID) : 'user_id'] as Map
      }
    }
  }
}

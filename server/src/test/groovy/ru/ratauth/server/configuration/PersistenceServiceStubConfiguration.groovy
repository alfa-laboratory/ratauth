package ru.ratauth.server.configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import ru.ratauth.entities.AuthClient
import ru.ratauth.entities.AuthEntry
import ru.ratauth.entities.RelyingParty
import ru.ratauth.entities.Session
import ru.ratauth.entities.Status
import ru.ratauth.entities.Token
import ru.ratauth.entities.TokenCache
import ru.ratauth.exception.ExpiredException
import ru.ratauth.server.utils.DateUtils
import ru.ratauth.services.ClientService
import ru.ratauth.services.SessionService
import ru.ratauth.services.TokenCacheService
import rx.Observable

import java.time.LocalDateTime

/**
 * @author mgorelikov
 * @since 25/02/16
 */
@Configuration
class PersistenceServiceStubConfiguration {
  public static final String CLIENT_SECRET = 'HdC4t2Wpjn/obYj9JHLVwmGzSqQ5SlatYqMF6zuAL0s='
  public static final String CLIENT_NAME = 'mine'
  public static final String PASSWORD = 'password'
  public static final String TOKEN = '1234'
  public static final String REFRESH_TOKEN = '12345'
  public static final String CODE = '123'

  private static final LocalDateTime NOW = LocalDateTime.now()
  private static final LocalDateTime TOMORROW = NOW.plusDays(1)

  @Bean
  @Primary
  public ClientService relyingPartyService() {
    return new ClientService() {
      @Override
      Observable<RelyingParty> getRelyingParty(String name) {
        if (name == CLIENT_NAME)
          return Observable.just(new RelyingParty(
              id: 'id',
              name: CLIENT_NAME,
              identityProvider: 'STUB',
              secret: CLIENT_SECRET,
              password: PASSWORD,
              codeTTL: 36000l,
              refreshTokenTTL: 36000l,
              sessionTTL: 36000l,
              tokenTTL: 36000l
          )
          )
        else
          return Observable.just(new RelyingParty(
              id: 'id2',
              name: CLIENT_NAME + '2',
              identityProvider: 'STUB2',
              secret: CLIENT_SECRET,
              password: PASSWORD,
              codeTTL: 36000l,
              refreshTokenTTL: 36000l,
              sessionTTL: 36000l,
              tokenTTL: 36000l
          )
          )
      }

      @Override
      Observable<AuthClient> getClient(String name) {
        if (name == CLIENT_NAME)
          return Observable.just(new AuthClient(
              id: 'id',
              name: CLIENT_NAME,
              secret: CLIENT_SECRET,
              password: PASSWORD
          ))
        else
          return Observable.just(new AuthClient(
              id: 'id',
              name: CLIENT_NAME + '3',
              secret: CLIENT_SECRET,
              password: PASSWORD
          ))
      }
    }
  }

  @Bean
  @Primary
  public TokenCacheService tokenCacheService() {
    return new TokenCacheService() {
      @Override
      Observable<TokenCache> create(TokenCache cache) {
        return Observable.just(cache)
      }

      @Override
      Observable<TokenCache> get(String token, String client) {
        return null
      }
    }
  }

  @Bean
  @Primary
  public SessionService authCodeService() {
    return new SessionService() {
      @Override
      Observable<Session> getByValidCode(String code, Date now) {
        if (code == CODE)
          return Observable.just(
              new Session(
                  identityProvider: 'STUB',
                  userInfo: 'eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwOi8vcmF0YXV0aC5ydSIsImlhdCI6MTQ1Nzg1NzAzOCwiZXhwIjoxNDg5MzkyODcxLCJhdWQiOiJzb21lLWFwcCIsInN1YiI6InVzZXJfaWQiLCJqdGkiOiJiZDYzNjkyOC03MTk2LTM5YTctODlmNi03OGY5NDY3NjU0ZWIiLCJycF9iYXNlX2FkZHJlc3MiOlsiaHR0cDovL3JhdGF1dGgucnUiLCJodHRwOi8vcmF0YXV0aC5ydSJdLCJ1c2VyX2lkIjoidXNlcl9pZCJ9.rqxqXV9X0kdjmyWxuVJkYU8sNC5sW9dC9NUqT-CodEM',
                  status: Status.ACTIVE,
                  entries: [
                      new AuthEntry(authCode: 'code',
                          relyingParty: CLIENT_NAME,
                          scopes: ['rs.read'] as Set,
                          refreshToken: REFRESH_TOKEN
                      )] as Set)
          )
        else
          return Observable.error(new ExpiredException('Auth code expired'))
      }


      @Override
      Observable<Session> getByValidRefreshToken(String token, Date now) {
        if (token == REFRESH_TOKEN)
          return Observable.just(
              new Session(
                  identityProvider: 'STUB',
                  userInfo: 'eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwOi8vcmF0YXV0aC5ydSIsImlhdCI6MTQ1Nzg1NzAzOCwiZXhwIjoxNDg5MzkyODcxLCJhdWQiOiJzb21lLWFwcCIsInN1YiI6InVzZXJfaWQiLCJqdGkiOiJiZDYzNjkyOC03MTk2LTM5YTctODlmNi03OGY5NDY3NjU0ZWIiLCJycF9iYXNlX2FkZHJlc3MiOlsiaHR0cDovL3JhdGF1dGgucnUiLCJodHRwOi8vcmF0YXV0aC5ydSJdLCJ1c2VyX2lkIjoidXNlcl9pZCJ9.rqxqXV9X0kdjmyWxuVJkYU8sNC5sW9dC9NUqT-CodEM',
                  status: Status.ACTIVE,
                  entries: [
                      new AuthEntry(authCode: 'code',
                          relyingParty: CLIENT_NAME,
                          scopes: ['rs.read'] as Set,
                          refreshToken: REFRESH_TOKEN,
                          tokens: [new Token(token: TOKEN,
                              expiresIn: DateUtils.fromLocal(TOMORROW),
                              created: new Date())] as Set
                      )] as Set)
          )
      }

      @Override
      Observable<Session> getByValidToken(String token, Date now) {
        if (token == TOKEN)
          return Observable.just(
              new Session(
                  identityProvider: 'STUB',
                  userInfo: 'eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwOi8vcmF0YXV0aC5ydSIsImlhdCI6MTQ1Nzg1NzAzOCwiZXhwIjoxNDg5MzkyODcxLCJhdWQiOiJzb21lLWFwcCIsInN1YiI6InVzZXJfaWQiLCJqdGkiOiJiZDYzNjkyOC03MTk2LTM5YTctODlmNi03OGY5NDY3NjU0ZWIiLCJycF9iYXNlX2FkZHJlc3MiOlsiaHR0cDovL3JhdGF1dGgucnUiLCJodHRwOi8vcmF0YXV0aC5ydSJdLCJ1c2VyX2lkIjoidXNlcl9pZCJ9.rqxqXV9X0kdjmyWxuVJkYU8sNC5sW9dC9NUqT-CodEM',
                  status: Status.ACTIVE,
                  entries: [
                      new AuthEntry(authCode: 'code',
                          relyingParty: CLIENT_NAME,
                          scopes: ['rs.read'] as Set,
                          refreshToken: REFRESH_TOKEN,
                          tokens: [new Token(token: TOKEN,
                              expiresIn: DateUtils.fromLocal(TOMORROW),
                              created: new Date())] as Set
                      )] as Set)
          )
      }

      @Override
      Observable<Session> create(Session session) {
        return Observable.just(session)
      }

      @Override
      Observable<Boolean> addEntry(String sessionId, AuthEntry entry) {
        return Observable.just(true)
      }

      @Override
      Observable<Boolean> addToken(String sessionId, String relyingParty, Token token) {
        return Observable.just(true)
      }

      @Override
      Observable<Boolean> invalidateSession(String sessionId) {
        return Observable.just(true)
      }

      @Override
      Observable<Boolean> invalidateForClient(String clientId) {
        return Observable.just(true)
      }
    }
  }
}

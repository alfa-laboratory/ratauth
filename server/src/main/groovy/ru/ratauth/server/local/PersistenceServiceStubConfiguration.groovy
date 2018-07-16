package ru.ratauth.server.local

import groovy.transform.CompileStatic
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import ru.ratauth.entities.*
import ru.ratauth.exception.ExpiredException
import ru.ratauth.services.DeviceInfoEventService
import ru.ratauth.server.utils.DateUtils
import ru.ratauth.server.utils.SecurityUtils
import ru.ratauth.services.ClientService
import ru.ratauth.services.DeviceInfoService
import ru.ratauth.services.SessionService
import ru.ratauth.services.TokenCacheService
import rx.Observable

import java.time.LocalDateTime
/**
 * @author mgorelikov
 * @since 25/02/16
 */
@CompileStatic
class PersistenceServiceStubConfiguration {
  public static final String CLIENT_SECRET = 'HdC4t2Wpjn/obYj9JHLVwmGzSqQ5SlatYqMF6zuAL0s='
  public static final String SESSION_TOKEN = 'session_token'
  public static final String CLIENT_NAME = 'mine'
  public static final String CLIENT_NAME_DUMMY = 'DummyIdentityProvider'
  public static final String CLIENT_NAME_REST = 'RestIdentityProvider'
  public static final String PASSWORD = 'password'
  public static final String SALT = 'JBn7SnEzMy0MXdNsh5GVvktSGuRs0+BNVZ47kmm3TDM='
  public static final String TOKEN = '1234'
  public static final String MFA_TOKEN = 'mfa-token-test'
  public static final String REFRESH_TOKEN_OLD_SCHEME = '12345'
  public static final String REFRESH_TOKEN = '123456'
  public static final String CODE = '123'
  public static final String CODE_EXPIRED = '1111'
  public static final String ID_TOKEN = 'eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwOi8vcmF0YXV0aC5ydSIsImlhdCI6MTQ1Nzg1NzAzOCwiZXhwIjoxNDg5MzkyODcxLCJhdWQiOiJzb21lLWFwcCIsInN1YiI6InVzZXJfaWQiLCJqdGkiOiJiZDYzNjkyOC03MTk2LTM5YTctODlmNi03OGY5NDY3NjU0ZWIiLCJycF9iYXNlX2FkZHJlc3MiOlsiaHR0cDovL3JhdGF1dGgucnUiLCJodHRwOi8vcmF0YXV0aC5ydSJdLCJ1c2VyX2lkIjoidXNlcl9pZCJ9.rqxqXV9X0kdjmyWxuVJkYU8sNC5sW9dC9NUqT-CodEM'

  private static final LocalDateTime NOW = LocalDateTime.now()
  private static final LocalDateTime TOMORROW = NOW.plusDays(1)

  @Bean
  @Primary
  ClientService relyingPartyService() {
    return new ClientService() {
      @Override
      Observable<RelyingParty> getRelyingParty(String name) {
        if (name == CLIENT_NAME)
          return Observable.just(new RelyingParty(
              id: 'id',
              name: CLIENT_NAME,
              identityProvider: 'STUB',
              secret: CLIENT_SECRET,
              password: SecurityUtils.hashPassword(PASSWORD, SALT),
              salt: SALT,
              codeTTL: 36000l,
              refreshTokenTTL: 36000l,
              sessionTTL: 36000l,
              tokenTTL: 36000l,
              redirectURIs: ['https://domain.mine', 'mine.domain'],
              registrationRedirectURI: 'http://domain.mine/oidc/register',
              authorizationRedirectURI: 'http://domain.mine/oidc/authorize',
              authorizationPageURI: 'http://domain.mine/oidc/web/authorize?is_webview=true',
              registrationPageURI: 'http://domain.mine/oidc//web/register?is_webview=true',
              incAuthLevelPageURI: 'http://domain.mine/oidc//web/inc_auth_level?is_webview=true'))
        else if (name == CLIENT_NAME_DUMMY)
          return Observable.just(new RelyingParty(
                  id: 'id',
                  name: CLIENT_NAME_DUMMY,
                  identityProvider: 'DummyIdentityProvider',
                  secret: CLIENT_SECRET,
                  password: SecurityUtils.hashPassword(PASSWORD, SALT),
                  salt: SALT,
                  codeTTL: 36000l,
                  refreshTokenTTL: 36000l,
                  sessionTTL: 36000l,
                  tokenTTL: 36000l,
                  redirectURIs: ['https://domain.mine', 'mine.domain'],
                  registrationRedirectURI: 'http://domain.mine/oidc/register',
                  authorizationRedirectURI: 'http://domain.mine/oidc/authorize',
                  authorizationPageURI: 'http://domain.mine/oidc/web/authorize?is_webview=true',
                  registrationPageURI: 'http://domain.mine/oidc/web/register?is_webview=true',
                  incAuthLevelPageURI: 'http://domain.mine/oidc/web/inc_auth_level?is_webview=true'))
        else if (name == CLIENT_NAME_REST)
          return Observable.just(new RelyingParty(
                  id: 'id',
                  name: CLIENT_NAME_DUMMY,
                  identityProvider: 'REST',
                  secret: CLIENT_SECRET,
                  password: SecurityUtils.hashPassword(PASSWORD, SALT),
                  salt: SALT,
                  codeTTL: 36000l,
                  refreshTokenTTL: 36000l,
                  sessionTTL: 36000l,
                  tokenTTL: 36000l,
                  redirectURIs: ['https://domain.mine', 'mine.domain'],
                  registrationRedirectURI: 'http://domain.mine/oidc/register',
                  authorizationRedirectURI: 'http://domain.mine/oidc/authorize',
                  authorizationPageURI: 'http://domain.mine/oidc/web/authorize?is_webview=true',
                  registrationPageURI: 'http://domain.mine/oidc/web/register?is_webview=true',
                  incAuthLevelPageURI: 'http://domain.mine/oidc/web/inc_auth_level?is_webview=true'))
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
              tokenTTL: 36000l,
              redirectURIs: ['https://domain.mine', 'mine.domain'],
              authorizationRedirectURI: 'http://domain.mine/oidc/authorize',
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
              salt: SALT,
              password: SecurityUtils.hashPassword(PASSWORD, SALT),
          ))
        else
          return Observable.just(new AuthClient(
              id: 'id',
              name: CLIENT_NAME + '3',
              secret: CLIENT_SECRET,
              password: PASSWORD
          ))
      }

      @Override
      Observable<SessionClient> getSessionClient(String name) {
        if (name == CLIENT_NAME)
          return Observable.just(new SessionClient(
            id: 'id',
            name: CLIENT_NAME,
            secret: CLIENT_SECRET,
            salt: SALT,
            password: SecurityUtils.hashPassword(PASSWORD, SALT),
          ))
        else
          return Observable.just(new SessionClient(
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
  TokenCacheService tokenCacheService() {
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
  SessionService authCodeService() {
    return new SessionService() {
      @Override
      Observable<Session> getByValidCode(String code, Date now) {
        if (code == CODE)
          return Observable.just(
              new Session(
                  id: 'id-1',
                  identityProvider: 'STUB',
                  sessionToken: SESSION_TOKEN,
                  userInfo: ID_TOKEN,
                  status: Status.ACTIVE,
                  expiresIn: DateUtils.fromLocal(LocalDateTime.now().plusDays(1)),
                  receivedAcrValues: AcrValues.valueOf("login:sms"),
                  entries: [
                      new AuthEntry(authCode: 'code',
                          relyingParty: CLIENT_NAME,
                          scopes: ['rs.read'] as Set,
                          refreshToken: REFRESH_TOKEN_OLD_SCHEME
                      )] as Set)
          )
        else
          return Observable.error(new ExpiredException(ExpiredException.ID.AUTH_CODE_EXPIRED))
      }


      @Override
      Observable<Session> getByValidRefreshToken(String token, Date now) {
        if (token in [REFRESH_TOKEN, REFRESH_TOKEN_OLD_SCHEME])
          return Observable.just(
              new Session(
                  id: 'id-2',
                  identityProvider: 'STUB',
                  sessionToken: SESSION_TOKEN,
                  userInfo: ID_TOKEN,
                  status: Status.ACTIVE,
                  expiresIn: DateUtils.fromLocal(LocalDateTime.now().plusDays(1)),
                  receivedAcrValues: AcrValues.valueOf("login:sms"),
                  entries: [
                      new AuthEntry(authCode: 'code',
                          relyingParty: CLIENT_NAME,
                          scopes: ['rs.read'] as Set,
                          refreshToken: REFRESH_TOKEN_OLD_SCHEME,
                          tokens: [new Token(token: TOKEN,
                              refreshToken: REFRESH_TOKEN,
                              refreshTokenExpiresIn: DateUtils.fromLocal(TOMORROW),
                              expiresIn: DateUtils.fromLocal(TOMORROW),
                              created: new Date())] as Set
                      )] as Set)
          )
      }

      @Override
      Observable<Session> getByValidSessionToken(String token, Date now) {
        if (token == SESSION_TOKEN)
          return Observable.just(
            new Session(
              id: 'id-3',
              identityProvider: 'STUB',
              sessionToken: SESSION_TOKEN,
              userInfo: ID_TOKEN,
              status: Status.ACTIVE,
              receivedAcrValues: AcrValues.valueOf("login:sms"),
              expiresIn: DateUtils.fromLocal(LocalDateTime.now().plusDays(1)),
              entries: [
                new AuthEntry(authCode: 'code',
                  codeExpiresIn: DateUtils.fromLocal(LocalDateTime.now().plusMinutes(3)),
                  relyingParty: CLIENT_NAME,
                  scopes: ['rs.read'] as Set,
                  refreshToken: REFRESH_TOKEN_OLD_SCHEME
                )] as Set)
          )
        else
          return Observable.error(new ExpiredException(ExpiredException.ID.AUTH_CODE_EXPIRED))
      }

      @Override
      Observable<Boolean> invalidateForUser(String userId, Date blocked) {
        return null
      }

      @Override
      Observable<Session> getByValidToken(String token, Date now) {
        if (token == TOKEN)
          return Observable.just(
              new Session(
                  id: 'id-4',
                  identityProvider: 'STUB',
                  sessionToken: SESSION_TOKEN,
                  userInfo: ID_TOKEN,
                  status: Status.ACTIVE,
                  expiresIn: DateUtils.fromLocal(LocalDateTime.now().plusDays(1)),
                  receivedAcrValues: AcrValues.valueOf("login:sms"),
                  entries: [
                      new AuthEntry(authCode: 'code',
                          relyingParty: CLIENT_NAME,
                          scopes: ['rs.read'] as Set,
                          refreshToken: REFRESH_TOKEN_OLD_SCHEME,
                          tokens: [new Token(token: TOKEN,
                              refreshToken: REFRESH_TOKEN,
                              refreshTokenExpiresIn: DateUtils.fromLocal(TOMORROW),
                              expiresIn: DateUtils.fromLocal(TOMORROW),
                              created: new Date())] as Set
                      )] as Set)
          )
      }

      @Override
      Observable<Session> getByValidMFAToken(String token, Date now) {
        if (token == MFA_TOKEN) {
          return Observable.just(
                  new Session(
                          id: 'id-5',
                          identityProvider: 'STUB',
                          sessionToken: SESSION_TOKEN,
                          userInfo: ID_TOKEN,
                          status: Status.ACTIVE,
                          mfaToken: MFA_TOKEN,
                          receivedAcrValues: AcrValues.valueOf("none"),
                          expiresIn: DateUtils.fromLocal(LocalDateTime.now().plusDays(1)),
                          entries: [
                                  new AuthEntry(
                                          codeExpiresIn: DateUtils.fromLocal(LocalDateTime.now().plusDays(1)),
                                          authCode: 'code',
                                          relyingParty: CLIENT_NAME,
                                          scopes: ['rs.read'] as Set,
                                          refreshToken: REFRESH_TOKEN_OLD_SCHEME,
                                          tokens: [new Token(token: TOKEN,
                                                  refreshToken: REFRESH_TOKEN,
                                                  refreshTokenExpiresIn: DateUtils.fromLocal(TOMORROW),
                                                  expiresIn: DateUtils.fromLocal(TOMORROW),
                                                  created: new Date())] as Set
                                  )] as Set)
          )
        }
      }

      @Override
      Observable<Boolean> updateAcrValues(Session session) {
        return Observable.just(true)
      }

      @Override
      Observable<Boolean> updateAuthCodeExpired(String code, Date now) {
        return Observable.just(true)
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
      Observable<Boolean> invalidateSession(String sessionId, Date blocked) {
        return null
      }

      @Override
      Observable<Boolean> invalidateForClient(String relyingParty, Date blocked) {
        return null
      }

      @Override
      Observable<Boolean> invalidateByRefreshToken(String relyingParty, String refreshToken) {
        return Observable.just(true)
      }

      @Override
      Observable<Boolean> invalidateByRefreshToken(String refreshToken) {
        return Observable.just(true)
      }

      @Override
      Observable<Boolean> updateCheckDate(String sessionId, Date lastCheck) {
        return null
      }

      @Override
      Observable<Boolean> updateUserInfo(String sessionId, String userInfo) {
        return Observable.just(true)
      }
    }
  }

  @Bean
  @Primary
  DeviceInfoService deviceInfoService() {
    return new DeviceInfoService() {
      @Override
      Observable<DeviceInfo> create(String clientId, String enroll, DeviceInfo deviceInfo) {
        return Observable.just(deviceInfo)
      }

      @Override
      Observable<List<DeviceInfo>> findByUserId(String userId) {
        return Observable.just([])
      }
    }
  }

  @Bean
  @Primary
  DeviceInfoEventService deviceInfoEventService() {
    return new DeviceInfoEventService() {
      @Override
      Observable<DeviceInfo> sendChangeDeviceInfoEvent(String clientId, String enroll, DeviceInfo oldDeviceInfo, DeviceInfo deviceInfo, Map<String, Object> userInfo) {
        return Observable.just(deviceInfo)
      }
    }
  }
}

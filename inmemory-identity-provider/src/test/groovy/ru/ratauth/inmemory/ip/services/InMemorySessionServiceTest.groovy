package ru.ratauth.inmemory.ip.services

import groovy.transform.CompileStatic
import org.apache.commons.lang3.time.DateUtils
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import ru.ratauth.entities.AuthEntry
import ru.ratauth.entities.Session
import ru.ratauth.entities.Status
import ru.ratauth.entities.Token
import ru.ratauth.exception.ExpiredException
import ru.ratauth.inmemory.ip.resource.ClassPathEntityResourceLoader
import ru.ratauth.inmemory.ip.resource.EntityParser
import ru.ratauth.inmemory.ip.resource.EntityResourceLoader
import ru.ratauth.services.SessionService

import static junit.framework.TestCase.assertEquals
import static junit.framework.TestCase.assertTrue

@CompileStatic
class InMemorySessionServiceTest {

  private static final String TEST_ID = 'test-id'
  private static final String SESSION_TOKEN = 'session_token'
  private static final String CLIENT_NAME = 'mine'
  private static final String TOKEN = '1234'
  private static final String REFRESH_TOKEN = '12345'
  private static final String CODE = '123'
  private static final String ID_TOKEN = 'eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwOi8vcmF0YXV0aC5ydSIsImlhdCI6MTQ1Nzg1NzAzOCwiZXhwIjoxNDg5MzkyODcxLCJhdWQiOiJzb21lLWFwcCIsInN1YiI6InVzZXJfaWQiLCJqdGkiOiJiZDYzNjkyOC03MTk2LTM5YTctODlmNi03OGY5NDY3NjU0ZWIiLCJycF9iYXNlX2FkZHJlc3MiOlsiaHR0cDovL3JhdGF1dGgucnUiLCJodHRwOi8vcmF0YXV0aC5ydSJdLCJ1c2VyX2lkIjoidXNlcl9pZCJ9.rqxqXV9X0kdjmyWxuVJkYU8sNC5sW9dC9NUqT-CodEM'
  private static final Date NOW = new Date()
  private static final Date YESTERDAY = DateUtils.addDays(NOW, -1)
  private static final Date TOMORROW = DateUtils.addDays(NOW, 1)

  @Rule
  public final ExpectedException expectedException = ExpectedException.none()

  private EntityParser entityParser
  private SessionService sessionService

  @Before
  void setUp() {
    EntityResourceLoader resource = new ClassPathEntityResourceLoader("")
    entityParser = new EntityParser(resource)
    sessionService = new InMemorySessionService(new ArrayList<Session>());
  }

  @Test
  void testCreateShouldShouldThrowNPEWhenSessionIsNull() {
    expectedException.expect(NullPointerException.class)
    expectedException.expectMessage("Session should not be null")

    sessionService.create(null)
  }

  @Test
  void testGetByValidCodeShouldSuccess() {
    Session session = createSession()
    sessionService.create(session)

    Session result = sessionService.getByValidCode(CODE, YESTERDAY).toBlocking().single()

    assertEquals(result, session)
  }

  @Test
  void testGetByValidCodeShouldThrowAuthCodeHasExpiredWhenSessionExpired() {
    expectedException.expect(ExpiredException.class)
    expectedException.expectMessage("Auth code has expired")
    Session session = createSession()
    sessionService.create(session)

    sessionService.getByValidCode(CODE, TOMORROW).toBlocking().single()
  }

  @Test
  void testGetByValidRefreshTokenShouldSuccess() {
    Session session = createSession()
    sessionService.create(session)

    Session result = sessionService.getByValidRefreshToken(REFRESH_TOKEN, YESTERDAY)
        .toBlocking()
        .single()

    assertEquals(result, session)
  }

  @Test
  void testGetByValidRefreshTokenShouldThrowAuthCodeHasExpiredWhenSessionExpired() {
    expectedException.expect(ExpiredException.class)
    expectedException.expectMessage("Auth code has expired")
    Session session = createSession()
    sessionService.create(session)

    sessionService.getByValidRefreshToken(REFRESH_TOKEN, TOMORROW).toBlocking().single()
  }

  @Test
  void testGetByValidSessionTokenShouldSuccess() {
    Session session = createSession()
    sessionService.create(session)

    Session result = sessionService.getByValidSessionToken(SESSION_TOKEN, YESTERDAY)
        .toBlocking()
        .single()

    assertEquals(result, session)
  }

  @Test
  void testGetByValidSessionTokenShouldThrowAuthCodeHasExpiredWhenSessionExpired() {
    expectedException.expect(ExpiredException.class)
    expectedException.expectMessage("Auth code has expired")
    Session session = createSession()
    sessionService.create(session)

    sessionService.getByValidSessionToken(SESSION_TOKEN, TOMORROW).toBlocking().single()
  }

  @Test
  void testGetByValidTokenShouldSuccess() {
    Session session = createSession()
    sessionService.create(session)

    Session result = sessionService.getByValidToken(TOKEN, YESTERDAY)
        .toBlocking()
        .single()

    assertEquals(result, session)
  }

  @Test
  void testGetByValidTokenShouldThrowAuthCodeHasExpiredWhenSessionExpired() {
    expectedException.expect(ExpiredException.class)
    expectedException.expectMessage("Auth code has expired")
    Session session = createSession()
    sessionService.create(session)

    sessionService.getByValidToken(TOKEN, TOMORROW).toBlocking().single()
  }

  @Test
  void testAddEntrySuccess() {
    Session session = createSession()
    AuthEntry authEntry = new AuthEntry(authCode: "789",
        relyingParty: CLIENT_NAME,
        scopes: ['rs.read'] as Set,
        refreshToken: REFRESH_TOKEN,
    )

    sessionService.create(session)
    assertEquals(1, session.getEntries().size())

    sessionService.addEntry(session.getId(), authEntry)

    assertEquals(2, session.getEntries().size())
  }

  @Test
  void testAddEntryAuthCodeHasExpiredWhenSessionDoesNotExist() {
    expectedException.expect(ExpiredException.class)
    expectedException.expectMessage("Auth code has expired")

    AuthEntry authEntry = new AuthEntry(authCode: "789",
        relyingParty: CLIENT_NAME,
        scopes: ['rs.read'] as Set,
        refreshToken: REFRESH_TOKEN,
    )

    sessionService.addEntry(TEST_ID, authEntry)
  }

  @Test
  void addTokenSuccess() {
    Session session = createSession()
    Token token = new Token(
        token: 'test-token',
        expiresIn: NOW,
        created: YESTERDAY
    )
    sessionService.create(session)

    sessionService.addToken(session.getId(), CLIENT_NAME, token)
    assertTrue(session.getEntry(CLIENT_NAME).get().getTokens().contains(token))
  }

  @Test
  void testInvalidateSession() {
    Session session = createSession()
    sessionService.create(session)

    sessionService.invalidateSession(session.getId(), TOMORROW)

    assertEquals(TOMORROW, session.getBlocked())
  }

  @Test
  void testInvalidateForUser() {
    Session session = createSession()
    sessionService.create(session)

    sessionService.invalidateForUser(session.getUserId(), TOMORROW)

    assertEquals(TOMORROW, session.getBlocked())
  }

  @Test
  void testInvalidateForClient() {
    Session session = createSession()
    sessionService.create(session)

    sessionService.invalidateForClient(CLIENT_NAME, TOMORROW)

    assertEquals(TOMORROW, session.getBlocked())
  }

  @Test
  void testUpdateCheckDate() {
    Session session = createSession()
    sessionService.create(session)

    sessionService.updateCheckDate(session.getId(), YESTERDAY)

    assertEquals(YESTERDAY, session.getLastCheck())
  }

  private Session createSession() {
    new Session(
        id: TEST_ID,
        userId: "test_user_id",
        identityProvider: 'STUB',
        sessionToken: SESSION_TOKEN,
        userInfo: ID_TOKEN,
        status: Status.ACTIVE,
        expiresIn: NOW,
        entries: [
            new AuthEntry(authCode: CODE,
                relyingParty: CLIENT_NAME,
                scopes: ['rs.read'] as Set,
                refreshToken: REFRESH_TOKEN,
                tokens: [
                    new Token(
                        token: TOKEN,
                        expiresIn: NOW,
                        created: YESTERDAY
                    )
                ] as Set
            )] as Set)
  }

}

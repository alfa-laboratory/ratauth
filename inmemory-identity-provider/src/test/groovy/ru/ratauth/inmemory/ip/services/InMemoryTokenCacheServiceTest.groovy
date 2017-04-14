package ru.ratauth.inmemory.ip.services

import groovy.transform.CompileStatic
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import ru.ratauth.entities.TokenCache
import ru.ratauth.services.TokenCacheService

import java.text.SimpleDateFormat

import static junit.framework.Assert.assertEquals

@CompileStatic
class InMemoryTokenCacheServiceTest {

  @Rule
  public final ExpectedException expectedException = ExpectedException.none()
  private TokenCacheService tokenCacheService

  @Before
  void setUp() {
    tokenCacheService = new InMemoryTokenCacheService([createTokenCache()])
  }

  @Test
  void getTokenCacheSuccess() throws Exception {
    TokenCache tokenCache = tokenCacheService.get("test-token", "test-client-name").toBlocking().single()

    assertEquals(createTokenCache(), tokenCache)
  }

  @Test
  void createShouldThrowNPEWhenTokenIsNull() {
    expectedException.expect(NullPointerException.class)
    expectedException.expectMessage("Token cache should not be null")

    tokenCacheService.create(null)
  }

  @Test
  void getTokenCacheThrowIllegalArgumentExceptionWhenTokenDoesNotExist() throws Exception {
    expectedException.expect(IllegalArgumentException.class)

    tokenCacheService.get("fake-token", "fake-client-name").toBlocking().single()
  }

  private TokenCache createTokenCache() {
    return new TokenCache(
        token: "test-token",
        idToken: "test-id-token",
        session: "test-session",
        created: new SimpleDateFormat("yyyy-MM-dd").parse("2017-04-04"),
        client: "test-client-name"
    )
  }

}

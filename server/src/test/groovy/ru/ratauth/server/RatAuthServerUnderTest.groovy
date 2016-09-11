package ru.ratauth.server

import org.springframework.boot.test.IntegrationTest
import org.springframework.boot.test.SpringApplicationConfiguration
import org.springframework.test.context.TestPropertySource
import ratpack.test.MainClassApplicationUnderTest

/**
 * @author mgorelikov
 * @since 03/11/15
 */
class RatAuthServerUnderTest extends MainClassApplicationUnderTest {
  RatAuthServerUnderTest() {
    super(RatAuthApplication.class)
  }
}

package ru.ratauth.server

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

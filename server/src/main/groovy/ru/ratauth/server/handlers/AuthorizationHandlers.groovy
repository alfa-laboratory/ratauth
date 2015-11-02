package ru.ratauth.server.handlers

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ratpack.func.Action
import ratpack.handling.Chain

/**
 * @author mgorelikov
 * @since 30/10/15
 */
@Configuration
class AuthorizationHandlers {
  @Bean
  Action<Chain> authChain() {
    chain {
      prefix('oauth') {

      }
    }
  }
}

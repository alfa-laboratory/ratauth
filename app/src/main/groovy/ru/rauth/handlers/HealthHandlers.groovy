package ru.rauth.handlers

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ratpack.func.Action
import ratpack.handling.Chain
import ratpack.handling.Context

import static ratpack.groovy.Groovy.chain
import static ratpack.jackson.Jackson.json

/**
 * @author tolkv
 * @since 30/10/15
 */
@Configuration
class HealthHandlers {
  @Bean
  Action<Chain> healthHandler() {
    chain {
      prefix('health') {
        all { Context ctx ->
          ctx.render json([
              status: 'UP'
          ])
        }
      }
    }
  }
}

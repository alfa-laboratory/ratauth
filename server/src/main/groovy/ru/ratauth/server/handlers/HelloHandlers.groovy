package ru.ratauth.server.handlers

import groovy.util.logging.Slf4j
import org.slf4j.Logger
import org.slf4j.LoggerFactory
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
class HelloHandlers {

  Logger log = LoggerFactory.getLogger(HelloHandlers)

  @Bean
  Action<Chain> hello() {
    chain {
      prefix('hello') {
        get { Context ctx ->
          this.log.info 'hello'
          ctx.render 'hello22ddsdfs'
        }

        prefix(':username') {
          get('say') { Context ctx ->
            def username = ctx.

            ctx.render json([
                say: 'hello' + username]
            )
          }
        }
      }

      prefix('hell') {
        all { Context ctx ->
          this.log.info 'hell'
          ctx.render json([isHell: 'all is hell'])
        }
      }
    }
  }
}

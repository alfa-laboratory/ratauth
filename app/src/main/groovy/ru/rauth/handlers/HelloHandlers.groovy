package ru.rauth.handlers

import groovy.util.logging.Slf4j
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
@Slf4j
@Configuration
class HelloHandlers {
  @Bean
  Action<Chain> hello() {
    chain {
      prefix('hello') {
        get { Context ctx ->
          log.info 'hello'
          ctx.render 'hello22ddsdfs'
        }

        prefix(':username') {
          get('say') { Context ctx ->
            def username = ctx.getAllPathTokens() get 'username'

            ctx.render json([
                say: 'hello' + username]
            )
          }
        }
      }

      prefix('hell') {
        all { Context ctx ->
          log.info 'hell'
          ctx.render json([isHell: 'all is hell'])
        }
      }
    }
  }
}

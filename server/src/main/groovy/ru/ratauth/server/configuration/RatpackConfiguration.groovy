package ru.ratauth.server.configuration
import groovy.transform.CompileStatic
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ratpack.func.Action
import ratpack.handling.Chain
import ratpack.handling.Context
import ratpack.spring.config.EnableRatpack

import static ratpack.groovy.Groovy.chain
/**
 * @author tolkv
 * @since 30/10/15
 */
@CompileStatic
@Configuration
class RatpackConfiguration {

  Logger log = LoggerFactory.getLogger(RatpackConfiguration)

//  @Bean
//  Action<Chain> all() {
//    chain {
//      all { Context ctx ->
//        log.info 'all handler'
//        ctx.next()
//      }
//    }
//  }
//
//  @Bean
//  Action<Chain> home() {
//    chain {
//      prefix('all') {
//        all { Context ctx ->
//          log.info 'all'
//          ctx.render 'hello'
//        }
//      }
//    }
//  }
}

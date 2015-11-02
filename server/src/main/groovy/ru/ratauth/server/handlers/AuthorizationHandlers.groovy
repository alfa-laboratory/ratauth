package ru.ratauth.server.handlers

import io.netty.handler.codec.http.HttpResponseStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ratpack.exec.Blocking
import ratpack.func.Action
import ratpack.handling.Chain
import ratpack.handling.Context
import ru.ratauth.server.services.AuthorizeService
import ratpack.http.Status;

/**
 * @author mgorelikov
 * @since 30/10/15
 */
@Configuration
class AuthorizationHandlers {
  @Autowired
  private AuthorizeService authorizeService

  @Bean
  Action<Chain> authChain() {
    chain {
      prefix('oauth/authorize') {
        get { Context ctx ->
          Blocking.get {
            authorizeService.authenticate(ctx.request)
          } then { res -> ctx.redirect(HttpResponseStatus.FOUND.code() ,authorizeService.authenticate(ctx.request)) }
        }
      }
    }
  }
}

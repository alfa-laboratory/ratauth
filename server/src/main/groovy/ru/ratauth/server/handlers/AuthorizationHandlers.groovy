package ru.ratauth.server.handlers

import io.netty.handler.codec.http.HttpResponseStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ratpack.exec.Blocking
import ratpack.func.Action
import ratpack.handling.Chain
import ratpack.handling.Context
import ru.ratauth.server.handlers.dto.CheckTokenDTO
import ru.ratauth.server.handlers.dto.TokenDTO
import ru.ratauth.server.services.AuthTokenService
import ru.ratauth.server.services.AuthorizeService

import static ratpack.groovy.Groovy.chain
import static ratpack.jackson.Jackson.json


/**
 * @author mgorelikov
 * @since 30/10/15
 */
@Configuration
class AuthorizationHandlers {
  @Autowired
  private AuthorizeService authorizeService
  @Autowired
  private AuthTokenService authTokenService

  @Bean
  Action<Chain> authChain() {
    chain {
      prefix('oauth') {
        prefix('authorize') {
          get { Context ctx ->
            Blocking.get {
              authorizeService.authenticate(ctx.request)
            } then { res -> ctx.redirect(HttpResponseStatus.FOUND.code() ,authorizeService.authenticate(ctx.request)) }
          }
        }

        prefix('token') {
          post { Context ctx ->
            Blocking.get {
              authTokenService.getToken(ctx.request)
            } then { res -> ctx.render json(new TokenDTO(res)) }
          }
        }
      }

      prefix('check_token') {
        post { Context ctx ->
          Blocking.get {
            authTokenService.checkToken(ctx.request)
          } then { res -> ctx.render json(new CheckTokenDTO(res)) }
        }
      }
    }
  }
}

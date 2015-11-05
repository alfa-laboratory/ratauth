package ru.ratauth.server.handlers

import io.netty.handler.codec.http.HttpResponseStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ratpack.exec.Blocking
import ratpack.exec.Promise
import ratpack.form.Form
import ratpack.func.Action
import ratpack.handling.Chain
import ratpack.handling.Context
import ru.ratauth.server.handlers.dto.CheckTokenDTO
import ru.ratauth.server.handlers.dto.TokenDTO
import ru.ratauth.server.services.AuthTokenService
import ru.ratauth.server.services.AuthorizeService

import static ratpack.groovy.Groovy.chain
import static ratpack.jackson.Jackson.json
import static ru.ratauth.server.handlers.readers.TokenRequestReader.*
import static ru.ratauth.server.handlers.readers.AuthzRequestReader.*


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
        path('authorize') {  Context ctx ->
          byMethod {
            get {
              def authorizeService = ctx.get(AuthorizeService.class)
              Blocking.get {
                authorizeService.authenticate readAuthzRequest(request.queryParams)
              } then { res -> ctx.redirect(HttpResponseStatus.FOUND.code() ,res.buildURL()) }
            }
            post {
              def authorizeService = ctx.get(AuthorizeService.class)
              Promise<Form> form = parse(Form.class);
              form.then { Form params ->
                Blocking.get {
                  authorizeService.authenticate readAuthzRequest(params)
                } then { res -> ctx.redirect(HttpResponseStatus.FOUND.code() ,res.buildURL()) }
              }
            }
          }
        }

        prefix('token') {
          post { Context ctx ->
            def authTokenService = ctx.get(AuthTokenService.class)
            Promise<Form> form = ctx.parse(Form.class);
            form.then { Form params ->
              Blocking.get {
                authTokenService.getToken readTokenRequest(params, ctx.request.headers)
              } then { res -> ctx.render json(new TokenDTO(res)) }
            }
          }
        }
      }

      prefix('check_token') {
        post { Context ctx ->
          def authTokenService = ctx.get(AuthTokenService.class)
          Promise<Form> form = ctx.parse(Form.class);
          form.then { Form params ->
            Blocking.get {
              authTokenService.checkToken readCheckTokenRequest(params, ctx.request.headers)
            } then { res -> ctx.render json(new CheckTokenDTO(res)) }
          }
        }
      }
    }
  }
}

package ru.ratauth.server.handlers

import groovy.util.logging.Slf4j
import io.netty.handler.codec.http.HttpResponseStatus
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
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
import static ratpack.rx.RxRatpack.observe
import static ru.ratauth.server.handlers.readers.TokenRequestReader.*
import static ru.ratauth.server.handlers.readers.AuthzRequestReader.*

import static ratpack.groovy.Groovy.groovyMarkupTemplate


/**
 * @author mgorelikov
 * @since 30/10/15
 */
@Slf4j
@Configuration
class AuthorizationHandlers {

  @Bean
  Action<Chain> authChain() {
    chain {
      path('authorize') { Context ctx ->
        byMethod {
          get {
            def authorizeService = ctx.get(AuthorizeService.class)
            authorizeService.authenticate readAuthzRequest(request.queryParams, ctx.request.headers) subscribe {
              res -> ctx.redirect(HttpResponseStatus.FOUND.code(), res.buildURL())
            }
          }
          post {
            def authorizeService = ctx.get(AuthorizeService.class)
            Promise<Form> formPromise = parse(Form.class);
            observe(formPromise).flatMap { params ->
              authorizeService.authenticate readAuthzRequest(params, ctx.request.headers)
            } subscribe {
              res -> ctx.redirect(HttpResponseStatus.FOUND.code(), res.buildURL())
            }
          }
        }
      }

      prefix('token') {
        post { Context ctx ->
          def authTokenService = ctx.get(AuthTokenService.class)
          Promise<Form> formPromise = ctx.parse(Form.class);
          observe(formPromise).flatMap { params ->
            authTokenService.getToken readTokenRequest(params, ctx.request.headers)
          } subscribe {
            res -> ctx.render json(new TokenDTO(res))
          }
        }
      }

      prefix('check_token') {
        post { Context ctx ->
          def authTokenService = ctx.get(AuthTokenService.class)
          Promise<Form> formPromise = ctx.parse(Form.class);
          observe(formPromise).flatMap { params ->
            authTokenService.checkToken readCheckTokenRequest(params, ctx.request.headers)
          } subscribe {
            res -> ctx.render json(new CheckTokenDTO(res))
          }
        }
      }

      prefix('login') {
        get { Context ctx ->
          render groovyMarkupTemplate('login.gtpl',
              title: 'Authorization',
              error: null,
              method: 'post',
              action: '/authorize',
              audValue: request.queryParams.get('aud'),
              scope: request.queryParams.get('scope'),
              clientId: request.queryParams.get('client_id'),
              responseType: 'code',
              redirectUri: request.queryParams.get('redirect_uri')
          )
        }
      }

      fileSystem 'public', { f -> f.files() }
    }
  }

}
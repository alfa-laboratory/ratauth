package ru.ratauth.server.handlers

import groovy.util.logging.Slf4j
import io.netty.handler.codec.http.HttpResponseStatus
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ratpack.error.ServerErrorHandler
import ratpack.exec.Promise
import ratpack.form.Form
import ratpack.func.Action
import ratpack.handling.Chain
import ratpack.handling.Context
import ru.ratauth.interaction.GrantType
import ru.ratauth.providers.registrations.dto.RegResult
import ru.ratauth.server.handlers.dto.CheckTokenDTO
import ru.ratauth.server.handlers.dto.RegisterDTO
import ru.ratauth.server.handlers.dto.TokenDTO
import ru.ratauth.server.services.AuthTokenService
import ru.ratauth.server.services.AuthorizeService
import ru.ratauth.server.services.RegistrationService

import static ratpack.groovy.Groovy.chain
import static ratpack.groovy.Groovy.groovyMarkupTemplate
import static ratpack.jackson.Jackson.json
import static ratpack.rx.RxRatpack.observe
import static ru.ratauth.server.handlers.readers.AuthzRequestReader.readAuthzRequest
import static ru.ratauth.server.handlers.readers.RegistrationRequestReader.readRegistrationRequest
import static ru.ratauth.server.handlers.readers.TokenRequestReader.readCheckTokenRequest
import static ru.ratauth.server.handlers.readers.TokenRequestReader.readTokenRequest


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
            authorizeService.authenticate(readAuthzRequest(request.queryParams, ctx.request.headers))
            .doOnError {
              throwable ->
              ctx.get(ServerErrorHandler.class).error(ctx, throwable)
            } subscribe {
              res -> ctx.redirect(HttpResponseStatus.FOUND.code(), res.buildURL())
            }
          }
          post {
            def authorizeService = ctx.get(AuthorizeService.class)
            Promise<Form> formPromise = parse(Form.class);
            observe(formPromise).flatMap { params ->
              def authRequest = readAuthzRequest(params, ctx.request.headers)
              if(GrantType.AUTHENTICATION_TOKEN == authRequest.grantType)
                authorizeService.crossAuthenticate(authRequest)
              else
                authorizeService.authenticate(authRequest)
            } doOnError { throwable ->
              ctx.get(ServerErrorHandler.class).error(ctx, throwable)
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
          } doOnError { throwable ->
            ctx.get(ServerErrorHandler.class).error(ctx, throwable)
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
          } doOnError { throwable ->
            ctx.get(ServerErrorHandler.class).error(ctx, throwable)
          } subscribe {
            res -> ctx.render json(new CheckTokenDTO(res))
          }
        }
      }

      prefix('register') {
        post { Context ctx ->
          def registerService = ctx.get(RegistrationService.class)
          Promise<Form> formPromise = ctx.parse(Form.class);
          observe(formPromise).flatMap { params ->
            def request = readRegistrationRequest(params, ctx.request.headers)
            if (GrantType.AUTHORIZATION_CODE == request.getGrantType())
              registerService.finishRegister(request)
            else
              registerService.register(request)
          } doOnError { throwable ->
              ctx.get(ServerErrorHandler.class).error(ctx, throwable)
          } subscribe {
            res ->
              if (res instanceof RegResult)
                ctx.render json(new RegisterDTO(res))
              else
                ctx.render json(new TokenDTO(res))
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

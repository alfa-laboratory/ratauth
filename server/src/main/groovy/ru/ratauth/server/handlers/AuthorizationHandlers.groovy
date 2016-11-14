package ru.ratauth.server.handlers

import io.netty.handler.codec.http.HttpResponseStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import ratpack.error.ServerErrorHandler
import ratpack.form.Form
import ratpack.func.Action
import ratpack.handling.Chain
import ratpack.handling.Context
import ru.ratauth.interaction.AuthzRequest
import ru.ratauth.interaction.GrantType
import ru.ratauth.server.services.AuthClientService
import ru.ratauth.server.services.AuthorizeService
import rx.Observable

import static ratpack.rx.RxRatpack.observe
import static ru.ratauth.server.handlers.readers.AuthzRequestReader.readAuthzRequest
import static ru.ratauth.server.handlers.readers.AuthzRequestReader.readClientId
/**
 * @author mgorelikov
 * @since 30/10/15
 */
@Component
class AuthorizationHandlers implements Action<Chain> {

  @Autowired
  private AuthorizeService authorizeService
  @Autowired
  private AuthClientService authClientService

  @Override
  void execute(Chain chain) throws Exception {
    chain.path('authorize') { Context ctx ->
      ctx.byMethod { meth ->
        meth.get { // GET
          ctx.byContent { cont ->
            cont.html { // REDIRECT
              redirectToWeb(ctx)
            } noMatch { // PROCESS AUTHORIZATION
              def requestObs = Observable.just(readAuthzRequest(ctx.request.queryParams, ctx.request.headers))
              requestObs.bindExec()
              authorize(ctx, requestObs)
            }
          }
        } post { // POST
          def queryParams = ctx.parse(Form)
          def requestObs = observe(queryParams).map { res -> readAuthzRequest(res, ctx.request.headers) }
          authorize(ctx, requestObs)
        }
      }
    }
  }

  private void redirectToWeb(Context ctx) {
    def clientId = readClientId(ctx.request.queryParams)
    def pageURIObs = authClientService.getAuthorizationPageURI(clientId, ctx.request.query)
    pageURIObs.bindExec()
    pageURIObs.subscribe {
      res -> ctx.redirect(HttpResponseStatus.MOVED_PERMANENTLY.code(), res)
    }
  }

  private void authorize(Context ctx, Observable<AuthzRequest> requestObs) {
    requestObs.flatMap { authRequest ->
      if (GrantType.AUTHENTICATION_TOKEN == authRequest.grantType) {
        authorizeService.crossAuthenticate(authRequest)
      } else {
        authorizeService.authenticate(authRequest)
      }
    } subscribe({
          res -> ctx.redirect(HttpResponseStatus.FOUND.code(), res.buildURL())
        }, {  /*on error*/
          throwable -> ctx.get(ServerErrorHandler).error(ctx, throwable)
        }
    )
  }
}

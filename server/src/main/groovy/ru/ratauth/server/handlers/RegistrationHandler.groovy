package ru.ratauth.server.handlers

import io.netty.handler.codec.http.HttpResponseStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import ratpack.error.ServerErrorHandler
import ratpack.exec.Promise
import ratpack.form.Form
import ratpack.func.Action
import ratpack.handling.Chain
import ratpack.handling.Context
import ru.ratauth.server.handlers.dto.TokenDTO
import ru.ratauth.server.services.AuthClientService
import ru.ratauth.server.services.RegistrationService
import rx.Subscription

import static ratpack.jackson.Jackson.json
import static ratpack.rx.RxRatpack.bindExec
import static ratpack.rx.RxRatpack.observe
import static ru.ratauth.server.handlers.readers.AuthzRequestReader.readClientId
import static ru.ratauth.server.handlers.readers.RegistrationRequestReader.readRegistrationRequest
/**
 * @author mgorelikov
 * @since 11/11/16
 * Handlers for registration method
 */
@Component
class RegistrationHandler implements Action<Chain> {

  @Autowired
  private RegistrationService registrationService
  @Autowired
  private AuthClientService authClientService

  @Override
  void execute(Chain chain) throws Exception {
    chain.path('register') { Context ctx ->
      ctx.byMethod { meth ->
        meth.get {
          ctx.byContent { spec ->
            spec.html {
              redirectToWeb(ctx)
            }.noMatch {
              initRegistration(ctx)
            }
          }
        } post {
          finishRegistration(ctx)
        }
      }
    }
  }

  // BY GET
  private Subscription initRegistration(Context ctx) {
    def parsedRequest = readRegistrationRequest(ctx.request.queryParams, ctx.request.headers)
    registrationService.register(parsedRequest).bindExec() subscribe({
      res -> ctx.redirect(HttpResponseStatus.FOUND.code(), res.buildURL())
    }, { /*on error*/
    throwable -> ctx.get(ServerErrorHandler).error(ctx, throwable)
    })
  }

  private Subscription finishRegistration(Context ctx) {
    Promise<Form> formPromise = ctx.parse(Form)
    observe(formPromise).flatMap { params ->
      def request = readRegistrationRequest(params, ctx.request.headers)
      registrationService.finishRegister(request)
    } subscribe({
          res -> ctx.render json(new TokenDTO(res))
        }, {  /*on error*/
          throwable -> ctx.get(ServerErrorHandler).error(ctx, throwable)
        }
    )
  }

  private void redirectToWeb(Context ctx) {
    def clientId = readClientId(ctx.request.queryParams)
    def pageURIObs = authClientService.getRegistrationPageURI(clientId, ctx.request.query)
    pageURIObs.bindExec()
    pageURIObs.subscribe {
      res -> ctx.redirect(HttpResponseStatus.MOVED_PERMANENTLY.code(), res)
    }
  }

}

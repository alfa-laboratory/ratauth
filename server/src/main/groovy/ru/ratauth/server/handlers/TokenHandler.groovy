package ru.ratauth.server.handlers

import org.apache.commons.lang3.tuple.ImmutablePair
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import ratpack.error.ServerErrorHandler
import ratpack.exec.Promise
import ratpack.form.Form
import ratpack.func.Action
import ratpack.handling.Chain
import ratpack.handling.Context
import ru.ratauth.interaction.TokenRequest
import ru.ratauth.interaction.TokenResponse
import ru.ratauth.server.handlers.dto.CheckTokenDTO
import ru.ratauth.server.handlers.dto.TokenDTO
import ru.ratauth.server.services.AuthTokenService
import ru.ratauth.services.SessionService

import static ratpack.jackson.Jackson.json
import static ratpack.rx.RxRatpack.observe
import static ru.ratauth.server.handlers.readers.TokenRequestReader.readCheckTokenRequest
import static ru.ratauth.server.handlers.readers.TokenRequestReader.readTokenRequest
/**
 * @author mgorelikov
 * @since 11/11/16
 */
@Component
class TokenHandler implements Action<Chain> {

  @Autowired
  private AuthTokenService authTokenService

  @Override
  void execute(Chain chain) throws Exception {
    chain.post('token') { Context ctx ->
      getToken(ctx)
    }.post('check_token') { Context ctx ->
      checkToken(ctx)
    }
  }

  private void checkToken(Context ctx) {
    Promise<Form> formPromise = ctx.parse(Form)
    observe(formPromise).flatMap { params ->
      authTokenService.checkToken readCheckTokenRequest(params, ctx.request.headers)
    } subscribe ({
        res -> ctx.render json(new CheckTokenDTO(res))
      }, { /*on error*/
        throwable -> ctx.get(ServerErrorHandler).error(ctx, throwable)
      }
    )
  }

  private void getToken(Context ctx) {
    Promise<Form> formPromise = ctx.parse(Form)
    observe(formPromise) map { params ->
        readTokenRequest(params, ctx.request.headers)
      } flatMap ({ request ->
        authTokenService.getToken(request)
      }, { TokenRequest req, TokenResponse resp ->
        ImmutablePair.of(req, resp)
      }) subscribe ({
        reqResp -> ctx.render json(new TokenDTO(reqResp.right, reqResp.left.responseTypes))
      }, { /*on error*/
        throwable -> ctx.get(ServerErrorHandler).error(ctx, throwable)
      }
    )
  }
}

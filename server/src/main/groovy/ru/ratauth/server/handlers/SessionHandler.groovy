package ru.ratauth.server.handlers

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import ratpack.error.ServerErrorHandler
import ratpack.exec.Promise
import ratpack.form.Form
import ratpack.func.Action
import ratpack.handling.Chain
import ratpack.handling.Context
import ru.ratauth.services.SessionService

import static ratpack.rx.RxRatpack.observe
import static ru.ratauth.server.handlers.readers.SessionRequestReader.readInvalidateByRefreshTokenRequest


@Component
class SessionHandler implements Action<Chain> {

    public static final String INVALIDATE_SESSION_BY_REFRESH_TOKEN = 'invalidate_session_by_refresh_token'

    @Autowired
    SessionService sessionService

    @Override
    void execute(Chain chain) throws Exception {
        chain.post(INVALIDATE_SESSION_BY_REFRESH_TOKEN) { Context ctx ->
            invalidateSessionByRefreshToken(ctx)
        }
    }

    private void invalidateSessionByRefreshToken(Context ctx) {
        Promise<Form> formPromise = ctx.parse(Form)
        observe(formPromise).flatMap { params ->
            def request = readInvalidateByRefreshTokenRequest(params, ctx.request.headers)
            sessionService.invalidateByRefreshToken(request.clientId, request.refreshToken)
        } subscribe({
            res -> ctx.response.status HttpStatus.OK.value() send()
        }, {
            throwable -> ctx.get(ServerErrorHandler).error(ctx, throwable)
        }
        )
    }
}

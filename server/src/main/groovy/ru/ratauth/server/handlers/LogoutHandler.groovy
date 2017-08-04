package ru.ratauth.server.handlers

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import ratpack.error.ServerErrorHandler
import ratpack.form.Form
import ratpack.func.Action
import ratpack.handling.Chain
import ratpack.handling.Context
import ru.ratauth.entities.AuthClient
import ru.ratauth.server.handlers.readers.RequestUtil
import ru.ratauth.server.services.AuthClientService
import ru.ratauth.services.SessionService
import rx.functions.Func2

import static ratpack.rx.RxRatpack.observe
import static rx.Observable.zip

@Component
class LogoutHandler implements Action<Chain> {

    @Autowired SessionService sessionService
    @Autowired AuthClientService clientService

    @Override
    void execute(Chain chain) throws Exception {
        chain.post('logout') { Context ctx ->
            def auth = RequestUtil.extractAuth(ctx.request.headers)
            observe(ctx.parse(Form))
                .flatMap( { request ->
                    return zip(
                            clientService.loadAndAuthClient(auth[0], auth[1], true),
                            sessionService.invalidateByRefreshToken(auth[0], request["refresh_token"] as String),
                            { AuthClient client, Boolean bool -> bool } as Func2)
                })
                .subscribe(
                    { Boolean res -> ctx.response.status HttpStatus.OK.value() send(); res },
                    { Throwable throwable -> ctx.get(ServerErrorHandler).error(ctx, throwable) })
        }
    }

}

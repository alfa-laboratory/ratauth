package ru.ratauth.server.handlers

import groovy.transform.CompileStatic
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import ratpack.error.ServerErrorHandler
import ratpack.form.Form
import ratpack.func.Action
import ratpack.handling.Chain
import ratpack.handling.Context
import ru.ratauth.server.handlers.readers.RequestUtil
import ru.ratauth.server.services.AuthClientService
import ru.ratauth.services.SessionService

import static ratpack.rx.RxRatpack.observe

@CompileStatic
class LogoutHandler implements Action<Chain> {

    @Autowired SessionService sessionService
    @Autowired AuthClientService clientService

    @Override
    void execute(Chain chain) throws Exception {
        chain.post('logout') { Context ctx ->
            def auth = RequestUtil.extractAuth(ctx.request.headers)
            observe(ctx.parse(Form))
                .flatMap( { request -> clientService.loadAndAuthClient(auth[0], auth[1], true); request })
                .flatMap({ request -> sessionService.invalidateByRefreshToken(auth[0], request["refresh_token"] as String) })
                .subscribe(
                    { Boolean res -> ctx.response.status HttpStatus.OK.value() send(); res },
                    { Throwable throwable -> ctx.get(ServerErrorHandler).error(ctx, throwable) })
        }
    }

}

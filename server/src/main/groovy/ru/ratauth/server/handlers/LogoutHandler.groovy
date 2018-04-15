package ru.ratauth.server.handlers

import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import ratpack.error.ServerErrorHandler
import ratpack.form.Form
import ratpack.func.Action
import ratpack.handling.Chain
import ratpack.handling.Context
import ru.ratauth.server.services.AuthClientService
import ru.ratauth.services.SessionService
import rx.functions.Func1

import static ratpack.rx.RxRatpack.observe

@Slf4j
@Component
class LogoutHandler implements Action<Chain> {

    @Autowired
    SessionService sessionService
    @Autowired
    AuthClientService clientService

    @Override
    void execute(Chain chain) throws Exception {
        chain.post('logout') { Context ctx ->
            observe(ctx.parse(Form))
                    .flatMap({ Form request ->
                return sessionService.invalidateByRefreshToken(request.refresh_token as String)
                        .map(
                        { Boolean bool ->
                            log.debug(/Logout event for session with refresh_token "${request.refresh_token}": $bool/)
                            return bool
                        } as Func1)
            })
                    .subscribe(
                    { Boolean res -> ctx.response.status HttpStatus.OK.value() send(); res },
                    { Throwable throwable -> ctx.get(ServerErrorHandler).error(ctx, throwable) })
        }
    }

}

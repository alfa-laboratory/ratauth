package ru.ratauth.server.extended.enroll.activate

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import ratpack.error.ServerErrorHandler
import ratpack.form.Form
import ratpack.func.Action
import ratpack.handling.Chain
import ratpack.handling.Context
import ratpack.jackson.Jackson
import ru.ratauth.server.handlers.readers.RequestReader
import rx.Observable
import rx.functions.Action1

import static ratpack.rx.RxRatpack.observe

@Component
public class ActivateEnrollHandler implements Action<Chain> {

    @Autowired ActivateEnrollService enrollService

    @Override
    void execute(Chain chain) throws Exception {
        chain.path('activate') { Context ctx ->
            ctx.byMethod { method ->
                method.post {
                    def parameters = ctx.parse(Form)
                    def request = observe(parameters)
                            .map { form -> new RequestReader(form) }
                            .map { requestReader -> readActivateEnrollRequest(requestReader) }
                    incAuthLevel(ctx, request)
                }
            }
        }
    }

    static ActivateEnrollRequest readActivateEnrollRequest(RequestReader params) {
        return new ActivateEnrollRequest(
                clientId: params.removeField("client_id", true),
                mfaToken: params.removeField("mfa_token", true),
                scope: params.removeField("scope", true).split(' ').toList(),
                authContext: params.removeField("acr_values", true).split(':').toList(),
                enroll: params.removeField("enroll", true).split(':').toList(),
                data: params.toMap()
        )
    }

    private void incAuthLevel(Context ctx, Observable<ActivateEnrollRequest> requestObservable) {
        requestObservable
                .flatMap({ request -> enrollService.incAuthLevel(request) })
                .map(Jackson.&json)
                .subscribe(ctx.&render, errorHandler(ctx))
    }

    static Action1<Throwable> errorHandler(Context ctx) {
        return { throwable -> ctx.get(ServerErrorHandler).error(ctx, throwable) } as Action1<Throwable>
    }

}

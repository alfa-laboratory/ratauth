package ru.ratauth.server.extended.enroll.activate


import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import ratpack.error.ServerErrorHandler
import ratpack.form.Form
import ratpack.func.Action
import ratpack.handling.Chain
import ratpack.handling.Context
import ratpack.jackson.Jackson
import ru.ratauth.entities.AcrValues
import ru.ratauth.server.handlers.readers.RequestReader
import rx.Observable
import rx.functions.Action1

import java.time.Instant

import static ratpack.rx.RxRatpack.observe

@Component
class ActivateEnrollHandler implements Action<Chain> {

    @Autowired
    ActivateEnrollService enrollService

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
        return ActivateEnrollRequest.builder()
                .clientId(params.removeField("client_id", true))
                .mfaToken(params.removeField("mfa_token", false))
                .scope(new HashSet<String>(Arrays.asList(params.removeField("scope", true).split(' '))))
                .authContext(AcrValues.valueOf(params.removeField("acr_values", true)))
                .enroll(AcrValues.valueOf(params.removeField("enroll", true)))
                .data(params.toMap())
                .deviceAppVersion(params.removeField("device_app_version", false))
                .deviceId(params.removeField("device_id", false))
                .deviceUUID(params.removeField("device_uuid", false))
                .deviceModel(params.removeField("device_model", false))
                .deviceGeo(params.removeField("device_geo", false))
                .deviceLocale(params.removeField("device_locale", false))
                .deviceCity(params.removeField("device_city", false))
                .deviceName(params.removeField("device_name", false))
                .deviceOSVersion(params.removeField("device_OS_version", false))
                .deviceBootTime(params.removeField("device_boot_time", false))
                .deviceTimezone(params.removeField("device_timezone", false))
                .deviceIp(params.removeField("device_ip", false))
                .deviceUserAgent(params.removeField("device_user_agent", false))
                .creationDate(Instant.now())
                .build()
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

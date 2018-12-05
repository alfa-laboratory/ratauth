package ru.ratauth.server.extended.enroll.verify

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import ratpack.error.ServerErrorHandler
import ratpack.form.Form
import ratpack.func.Action
import ratpack.handling.Chain
import ratpack.handling.Context
import ru.ratauth.server.handlers.readers.AuthzRequestReader
import ru.ratauth.server.handlers.readers.RequestReader
import rx.Observable
import rx.functions.Action1

import static ratpack.rx.RxRatpack.observe
import static ru.ratauth.server.handlers.readers.AuthzRequestReader.X_FORWARDED_FOR

@Component
class VerifyEnrollHandler implements Action<Chain> {

    private static final String ACR_SPLITTER = ':'
    private static final String STATE = 'state'

    @Autowired
    VerifyEnrollService enrollService

    @Override
    void execute(Chain chain) throws Exception {
        chain.path('verify') { Context ctx ->
            ctx.byMethod { method ->
                method.post {
                    def parameters = ctx.parse(Form)
                    def request = observe(parameters)
                            .map { form -> new RequestReader(form) }
                            .map { requestReader -> readVerifyEnrollRequest(requestReader) }
                    incAuthLevel(ctx, request)
                }
            }
        }
    }


    static VerifyEnrollRequest readVerifyEnrollRequest(RequestReader params) {
        return new VerifyEnrollRequest(
                clientId: params.removeField("client_id", true),
                state: params.removeField(STATE, false),
                mfaToken: params.removeField("mfa_token", true),
                redirectURI: params.removeField("redirect_uri", false),
                scope: params.removeField("scope", true).split(' ').toList(),
                authContext: params.removeField("acr_values", true).split(ACR_SPLITTER).toList(),
                enroll: params.removeField("enroll", true).split(ACR_SPLITTER).toList(),
                deviceAppVersion: params.removeField("device_app_version", false),
                deviceId: params.removeField("device_id", false),
                deviceUUID: params.removeField("device_uuid", false),
                deviceModel: params.removeField("device_model", false),
                deviceGeo: params.removeField("device_geo", false),
                deviceLocale: params.removeField("device_locale", false),
                deviceCity: params.removeField("device_city", false),
                deviceName: params.removeField("device_name", false),
                deviceOSVersion: params.removeField("device_os_version", false),
                deviceBootTime: params.removeField("device_boot_time", false),
                deviceTimezone: params.removeField("device_timezone", false),
                deviceIp: params.removeField("device_ip", false),
                deviceUserAgent: params.removeField("device_user_agent", false),
                xForwardedFor: params.removeField(X_FORWARDED_FOR, false),
                data: params.toMap()
        )
    }

    private void incAuthLevel(Context ctx, Observable<VerifyEnrollRequest> requestObservable) {
        requestObservable
                .flatMap({ request ->
            enrollService.incAuthLevel(request)
                    .map({ response -> response.putRedirectParameters(STATE, request.state); response })
        })
                .map({ response -> response.redirectURL })
                .subscribe({ response -> ctx.redirect(302, response) }, errorHandler(ctx))
    }

    static Action1<Throwable> errorHandler(Context ctx) {
        return { throwable -> ctx.get(ServerErrorHandler).error(ctx, throwable) } as Action1<Throwable>
    }
}

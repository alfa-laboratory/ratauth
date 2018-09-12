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
import ru.ratauth.entities.AuthEntry
import ru.ratauth.exception.AuthorizationException
import ru.ratauth.interaction.UpdateServiceRequest
import ru.ratauth.server.extended.update.UpdateFinishResponse
import ru.ratauth.server.handlers.readers.RequestReader
import ru.ratauth.server.services.AuthClientService
import ru.ratauth.services.SessionService
import ru.ratauth.services.UpdateDataService
import ru.ratauth.updateServices.UpdateService
import ru.ratauth.updateServices.dto.UpdateServiceInput
import rx.Observable
import rx.functions.Action1

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

import static java.time.LocalDateTime.now
import static ratpack.rx.RxRatpack.observe
import static ru.ratauth.exception.AuthorizationException.ID.AUTH_CODE_EXPIRES_IN_UPDATE_FAILED
import static ru.ratauth.server.handlers.readers.UpdateServiceRequestReader.readUpdateServiceRequest
import static ru.ratauth.server.utils.DateUtils.fromLocal

@Component
class UpdateHandler implements Action<Chain> {

    @Autowired
    private UpdateService updateService
    @Autowired
    private UpdateDataService updateDataService
    @Autowired
    private AuthClientService authClientService
    @Autowired
    private SessionService sessionService

    @Override
    void execute(Chain chain) throws Exception {
        chain.post('update') { ctx -> update(ctx) }
    }

    private void update(Context ctx) {
        Promise<Form> formPromise = ctx.parse(Form)
        observe(formPromise)
                .map { params -> new RequestReader(params) }
                .map { params -> readUpdateServiceRequest(params) }
                .map { request -> updateUserData(request, ctx) }
                .subscribe()
    }

    private void updateUserData(UpdateServiceRequest request, Context ctx) {
        updateDataService.getValidEntry(request.code)
            .subscribe {
            data ->
                updateDataService.invalidate(request.code).subscribe()
                updateService.update(UpdateServiceInput.builder()
                        .code(request.code)
                        .updateService(request.updateService)
                        .relyingParty(request.clientId)
                        .data(request.data)
                        .build())
                        .flatMap { r ->
                    def clientId = request.clientId
                    def sessionToken = data.sessionToken
                    //update code expired
                    makeFinishResponse(ctx, clientId, sessionToken)
                }.doOnError { errorHandler(ctx) }.subscribe()
        } {
            errorHandler(ctx)
        }
    }

    private void makeFinishResponse(Context ctx, String clientId, String sessionToken) {
        LocalDateTime now = now()
        authClientService.loadRelyingParty(clientId)
            .zipWith(getSession(sessionToken, clientId), { relyingParty, authEntry ->
                String authCode = authEntry.authCode
                LocalDateTime authCodeExpiresIn = now.plus(relyingParty.codeTTL, ChronoUnit.SECONDS)

                updateAuthCodeExpired(authCode, authCodeExpiresIn)

                long expiresIn = ChronoUnit.SECONDS.between(now, authCodeExpiresIn)

                def finishResponse = new UpdateFinishResponse(relyingParty.authorizationRedirectURI, sessionToken, authCode, expiresIn)
                ctx.redirect(HttpResponseStatus.FOUND.code(), finishResponse.redirectURL)
        }).subscribe()
    }

    private void updateAuthCodeExpired(String authCode, LocalDateTime authCodeExpiresIn) {
        sessionService.updateAuthCodeExpired(authCode, fromLocal(authCodeExpiresIn))
                .filter { it.booleanValue() }
                .switchIfEmpty(Observable.error(new AuthorizationException(AUTH_CODE_EXPIRES_IN_UPDATE_FAILED)))
                .subscribe()
    }

    private Observable<AuthEntry> getSession(String sessionToken, String clientId) {
        sessionService.getByValidSessionToken(sessionToken, fromLocal(now), false)
                .map { session -> session.getEntry(clientId).get() }
    }

    static Action1<Throwable> errorHandler(Context ctx) {
        return { throwable -> ctx.get(ServerErrorHandler).error(ctx, throwable) } as Action1<Throwable>
    }
}

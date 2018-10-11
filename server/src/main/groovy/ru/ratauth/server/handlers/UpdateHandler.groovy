package ru.ratauth.server.handlers

import com.fasterxml.jackson.databind.ObjectMapper
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import ratpack.error.ServerErrorHandler
import ratpack.exec.Promise
import ratpack.form.Form
import ratpack.func.Action
import ratpack.handling.Chain
import ratpack.handling.Context
import ru.ratauth.entities.AuthEntry
import ru.ratauth.entities.UpdateDataEntry
import ru.ratauth.exception.AuthorizationException
import ru.ratauth.interaction.UpdateServiceRequest
import ru.ratauth.server.extended.update.UpdateErrorResponse
import ru.ratauth.server.extended.update.UpdateFinishResponse
import ru.ratauth.server.handlers.readers.RequestReader
import ru.ratauth.server.services.AuthClientService
import ru.ratauth.services.SessionService
import ru.ratauth.services.UpdateDataService
import ru.ratauth.update.services.UpdateService
import ru.ratauth.update.services.dto.UpdateServiceInput
import ru.ratauth.update.services.dto.UpdateServiceResult
import rx.Observable

import java.time.LocalDateTime

import static io.netty.handler.codec.http.HttpResponseStatus.FOUND
import static io.netty.handler.codec.http.HttpResponseStatus.UNPROCESSABLE_ENTITY
import static java.time.LocalDateTime.now
import static java.time.temporal.ChronoUnit.SECONDS
import static ratpack.rx.RxRatpack.observe
import static ru.ratauth.exception.AuthorizationException.ID.AUTH_CODE_EXPIRES_IN_UPDATE_FAILED
import static ru.ratauth.server.handlers.readers.UpdateServiceRequestReader.readUpdateServiceRequest
import static ru.ratauth.server.utils.DateUtils.fromLocal
import static ru.ratauth.update.services.dto.UpdateServiceResult.Status.ERROR

@Slf4j
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
                .subscribe { request -> updateUserData(request, ctx) }
    }

    private void updateUserData(UpdateServiceRequest request, Context ctx) {
        updateDataService.getValidEntry(request.code).subscribe {
            updateDataEntry ->
                updateDataService.invalidate(request.code).subscribe()
                updateService.update(UpdateServiceInput.builder()
                        .code(request.code)
                        .updateService(request.updateService)
                        .relyingParty(request.clientId)
                        .data(request.data).build())
                        .flatMap { updateServiceResult ->

                    String clientId = request.clientId
                    String sessionToken = updateDataEntry.sessionToken

                    if (updateServiceResult.status == ERROR) {
                        doResponseWithError(ctx, sessionToken, updateServiceResult, updateDataEntry)
                    } else {
                        doFinishResponse(ctx, clientId, sessionToken)
                    }
                }
                .doOnError { throwable -> ctx.get(ServerErrorHandler).error(ctx, throwable) }.subscribe()
        } { throwable -> ctx.get(ServerErrorHandler).error(ctx, throwable) }
    }

    private void doResponseWithError(Context ctx, String sessionToken,
                                     UpdateServiceResult updateServiceResult,
                                     UpdateDataEntry updateDataEntry) {
        String reason = updateServiceResult.data['message'] as String
        getNewUpdateEntry(sessionToken, reason, updateDataEntry).subscribe {
            data ->
                updateDataService.getCode(sessionToken).subscribe {
                    code ->
                        ctx.response
                                .status(UNPROCESSABLE_ENTITY.code())
                                .send(makeJsonResponse(data.service, code, reason))

                        log.debug("new update code recieved = {}, sesson token is {}", code, sessionToken)
                }
                log.info("update handle validation error, new update code sent")
        }
    }

    private Observable<UpdateDataEntry> getNewUpdateEntry(String sessionToken,
                                                          String reason,
                                                          UpdateDataEntry updateDataEntry) {
        return updateDataService.create(sessionToken,
                reason,
                updateDataEntry.service,
                updateDataEntry.redirectUri,
                updateDataEntry.required)
    }

    private static String makeJsonResponse(String updateService, String updateCode, String reason) {
        return new ObjectMapper().writeValueAsString(UpdateErrorResponse.builder()
                .updateCode(updateCode)
                .updateService(updateService)
                .reason(reason).build())
    }

    private void doFinishResponse(Context ctx, String clientId, String sessionToken) {
        authClientService.loadRelyingParty(clientId)
                .zipWith(getSession(sessionToken, clientId),
                { relyingParty, authEntry ->

                    String authCode = authEntry.authCode
                    LocalDateTime now = now()
                    LocalDateTime authCodeExpiresIn = now.plus(relyingParty.codeTTL, SECONDS)

                    updateAuthCodeExpired(authCode, authCodeExpiresIn)

                    long expiresIn = SECONDS.between(now, authCodeExpiresIn)

                    def finishResponse = new UpdateFinishResponse(relyingParty.authorizationRedirectURI, sessionToken, authCode, expiresIn)
                    ctx.redirect(FOUND.code(), finishResponse.redirectURL)
                    log.debug("send redirect uri after update code {}", finishResponse.redirectURL)
                    log.info("update succeed")
                }).subscribe()
    }

    private void updateAuthCodeExpired(String authCode, LocalDateTime authCodeExpiresIn) {
        sessionService.updateAuthCodeExpired(authCode, fromLocal(authCodeExpiresIn))
                .filter { it.booleanValue() }
                .switchIfEmpty(Observable.error(new AuthorizationException(AUTH_CODE_EXPIRES_IN_UPDATE_FAILED)))
                .subscribe()
    }

    private Observable<AuthEntry> getSession(String sessionToken, String clientId) {
        sessionService.getByValidSessionToken(sessionToken, fromLocal(now()), false)
                .map { session -> session.getEntry(clientId).get() }
    }
}

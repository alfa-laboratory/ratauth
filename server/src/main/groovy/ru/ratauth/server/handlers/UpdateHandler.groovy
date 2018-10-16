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

import static io.netty.handler.codec.http.HttpResponseStatus.UNPROCESSABLE_ENTITY
import static java.time.LocalDateTime.now
import static java.time.temporal.ChronoUnit.SECONDS
import static org.springframework.http.HttpStatus.FOUND
import static ratpack.rx.RxRatpack.observe
import static ru.ratauth.exception.AuthorizationException.ID.AUTH_CODE_EXPIRES_IN_UPDATE_FAILED
import static ru.ratauth.server.handlers.readers.UpdateServiceRequestReader.toUpdateServiceRequest
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
                .map { params -> toUpdateServiceRequest(params) }
                .flatMap { request -> callRemoteUpdateService(request) }
                .flatMap { updateDataEntry, updateServiceRequest, updateServiceResult ->
            if (updateServiceResult.status == ERROR) {
                return tryRepeatUpdate(updateDataEntry, updateServiceResult)
                        .map {
                    newUpdateDataEntry ->
                        def jsonResponse = makeJsonResponse(newUpdateDataEntry.service, newUpdateDataEntry.code, newUpdateDataEntry.reason)
                        ctx.response.status(UNPROCESSABLE_ENTITY.code()).send(jsonResponse)
                        log.info("Update wasn't finished, one more attempt")
                        log.debug("New update attempt with response: {}", jsonResponse)

                }
            } else {
                return doSuccessResponse(updateDataEntry, updateServiceRequest)
                        .map { finishResponse ->
                    ctx.redirect(FOUND.value(), finishResponse.redirectURL)
                    log.info("Update succeed")
                    log.debug("Update succeed, send redirect uri after update code {}", finishResponse.redirectURL)
                }
            }
        }
        .subscribe({ log.info("Update call finished") },
                { throwable -> ctx.get(ServerErrorHandler).error(ctx, throwable) })
    }

    private callRemoteUpdateService(UpdateServiceRequest updateServiceRequest) {
        updateDataService.getValidEntry(updateServiceRequest.code)
                .flatMap { updateDataEntry ->
            updateDataService.invalidate(updateServiceRequest.code)
                    .filter { response -> response.booleanValue() }
                    .flatMap { response ->
            return updateService.update(UpdateServiceInput.builder()
                    .code(updateServiceRequest.code)
                    .updateService(updateServiceRequest.updateService)
                    .relyingParty(updateServiceRequest.clientId)
                    .data(updateServiceRequest.data).build())
                    .map { updateServiceResult -> [updateDataEntry, updateServiceRequest, updateServiceResult] }
        }
        }
    }

    private Observable<UpdateDataEntry> tryRepeatUpdate(UpdateDataEntry updateDataEntry, UpdateServiceResult updateServiceResult) {
        String sessionToken = updateDataEntry.sessionToken
        String reason = updateServiceResult.data['message']
        updateDataService.create(
                updateDataEntry.sessionToken,
                reason,
                updateDataEntry.service,
                updateDataEntry.redirectUri,
                updateDataEntry.required)
                .flatMap { newData ->
            return updateDataService.getCode(sessionToken)
                    .map { code ->
                log.debug("got new update code: {}", code)
                UpdateDataEntry.builder()
                        .service(newData.service)
                        .reason(newData.reason)
                        .code(code)
                        .build()
            }
        }
    }

    private Observable<UpdateFinishResponse> doSuccessResponse(UpdateDataEntry updateDataEntry, UpdateServiceRequest updateServiceRequest) {
        String sessionToken = updateDataEntry.sessionToken
        String clientId = updateServiceRequest.clientId
        return authClientService.loadRelyingParty(clientId)
                .zipWith(getSession(sessionToken, clientId), { relyingParty, authEntry -> [authEntry, relyingParty] })
                .flatMap { authEntry, relyingParty ->
            String authCode = authEntry.authCode
            LocalDateTime now = now()
            LocalDateTime authCodeExpiresIn = now.plus(relyingParty.codeTTL, SECONDS)
            updateAuthCodeExpired(authCode, authCodeExpiresIn)
                    .map { response ->
                long expiresIn = SECONDS.between(now, authCodeExpiresIn)
                return new UpdateFinishResponse(relyingParty.authorizationRedirectURI, sessionToken, authCode, expiresIn)
            }
        }
    }

    private static String makeJsonResponse(String updateService, String updateCode, String reason) {
        return new ObjectMapper().writeValueAsString(UpdateErrorResponse.builder()
                .updateCode(updateCode)
                .updateService(updateService)
                .reason(reason)
                .build())
    }

    private Observable<Boolean> updateAuthCodeExpired(String authCode, LocalDateTime authCodeExpiresIn) {
        sessionService.updateAuthCodeExpired(authCode, fromLocal(authCodeExpiresIn))
                .filter { it.booleanValue() }
                .switchIfEmpty(Observable.error(new AuthorizationException(AUTH_CODE_EXPIRES_IN_UPDATE_FAILED)))
    }

    private Observable<AuthEntry> getSession(String sessionToken, String clientId) {
        sessionService.getByValidSessionToken(sessionToken, fromLocal(now()), false)
                .map { session -> session.getEntry(clientId).get() }
    }
}

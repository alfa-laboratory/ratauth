package ru.ratauth.server.handlers


import io.netty.handler.codec.http.HttpResponseStatus
import org.apache.commons.lang3.tuple.ImmutableTriple
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import ratpack.error.ServerErrorHandler
import ratpack.exec.Promise
import ratpack.form.Form
import ratpack.func.Action
import ratpack.handling.Chain
import ratpack.handling.Context
import ru.ratauth.entities.UpdateDataEntry
import ru.ratauth.exception.AuthorizationException
import ru.ratauth.interaction.UpdateServiceRequest
import ru.ratauth.server.extended.update.UpdateFinishResponse
import ru.ratauth.server.handlers.readers.RequestReader
import ru.ratauth.server.services.AuthClientService
import ru.ratauth.server.updateServices.UpdateServiceResolver
import ru.ratauth.services.SessionService
import ru.ratauth.services.UpdateDataService
import ru.ratauth.updateServices.dto.UpdateServiceInput
import ru.ratauth.updateServices.dto.UpdateServiceOutput
import rx.Observable

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

import static ratpack.rx.RxRatpack.observe
import static ru.ratauth.server.handlers.readers.UpdateServiceRequestReader.readUpdateServiceRequest
import static ru.ratauth.server.utils.DateUtils.fromLocal
import static ru.ratauth.updateServices.dto.UpdateServiceOutput.Status.SKIPPED
import static ru.ratauth.updateServices.dto.UpdateServiceOutput.Status.SUCCESS

@Component
@SuppressWarnings(['UnusedPrivateField', 'UnusedVariable'])
class UpdateHandler implements Action<Chain> {

    @Autowired
    private UpdateServiceResolver updateServiceResolver
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
        .map { params -> readUpdateServiceRequest(params)
        } flatMap {
                //check code
            request ->
                updateDataService.getValidEntry(request.code)
                        .map { data ->
                                //invalidate code
                                updateDataService.invalidate(request.code).subscribe()
                                //update data
                                def response
                                if (!data.isRequired() && request.skip) {
                                    response = UpdateServiceOutput.builder().status(SKIPPED).build()
                                } else {
                                    response = updateServiceResolver.getUpdateService(request.updateService)
                                            .update(UpdateServiceInput.builder()
                                                .code(request.code)
                                                .relyingParty(request.clientId)
                                                .data(request.data)
                                                .build()).toBlocking().single()
                                }
                                new ImmutableTriple<UpdateServiceRequest, UpdateServiceOutput, UpdateDataEntry>(request, response, data)
                        }
        } filter {
            triple -> triple.middle.status == SUCCESS || triple.middle.status == SKIPPED
        } subscribe {
            triple ->
                def clientId = triple.left.clientId
                def sessionToken = triple.right.sessionToken
                //update code expired
                LocalDateTime now = LocalDateTime.now()
                def relyingParty = authClientService.loadRelyingParty(clientId).toBlocking().single()
                def authEntry = sessionService.getByValidSessionToken(sessionToken, fromLocal(now), false)
                        .map { session -> session.getEntry(clientId) }
                        .toBlocking().single().get()

                String authCode = authEntry.authCode
                LocalDateTime authCodeExpiresIn = now.plus(relyingParty.getCodeTTL(), ChronoUnit.SECONDS);

                sessionService.updateAuthCodeExpired(authCode, fromLocal(authCodeExpiresIn))
                        .filter { it.booleanValue() }
                        .switchIfEmpty(Observable.error(new AuthorizationException(AuthorizationException.ID.AUTH_CODE_EXPIRES_IN_UPDATE_FAILED)))
                        .subscribe()

                long expiresIn = ChronoUnit.SECONDS.between(now, authCodeExpiresIn)
                //send redirect with code --resp.buildURL()
                def finishResponse = new UpdateFinishResponse(relyingParty.authorizationRedirectURI, sessionToken, authCode, expiresIn)
                ctx.redirect(HttpResponseStatus.FOUND.code(), finishResponse.getRedirectURL())
        } {
            throwable -> ctx.get(ServerErrorHandler).error(ctx, throwable)
        }
    }
}
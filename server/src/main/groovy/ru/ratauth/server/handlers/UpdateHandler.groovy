package ru.ratauth.server.handlers

import io.netty.handler.codec.http.HttpResponseStatus
import org.apache.commons.lang3.tuple.ImmutableTriple
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import ratpack.error.ServerErrorHandler
import ratpack.exec.Promise
import ratpack.form.Form
import ratpack.func.Action
import ratpack.handling.Chain
import ratpack.handling.Context
import ru.ratauth.entities.AcrValue
import ru.ratauth.entities.UpdateDataEntry
import ru.ratauth.exception.AuthorizationException
import ru.ratauth.interaction.UpdateServiceRequest
import ru.ratauth.server.extended.update.UpdateFinishResponse
import ru.ratauth.server.handlers.readers.RequestReader
import ru.ratauth.server.services.AuthClientService
import ru.ratauth.services.SessionService
import ru.ratauth.services.UpdateDataService
import ru.ratauth.updateServices.UpdateService
import ru.ratauth.updateServices.dto.UpdateServiceInput
import ru.ratauth.updateServices.dto.UpdateServiceResult
import rx.Observable

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

import static ratpack.rx.RxRatpack.observe
import static ru.ratauth.exception.AuthorizationException.ID.AUTH_CODE_EXPIRES_IN_UPDATE_FAILED
import static ru.ratauth.server.handlers.readers.UpdateServiceRequestReader.readUpdateServiceRequest
import static ru.ratauth.server.utils.DateUtils.fromLocal
import static ru.ratauth.updateServices.dto.UpdateServiceResult.Status.SKIPPED
import static ru.ratauth.updateServices.dto.UpdateServiceResult.Status.SUCCESS

@Component
@SuppressWarnings(['AbcMetric'])
class UpdateHandler implements Action<Chain> {

    @Autowired
    private UpdateService updateService
    @Autowired
    private UpdateDataService updateDataService
    @Autowired
    private AuthClientService authClientService
    @Autowired
    private SessionService sessionService

    @Value('${acr_values.list::#{null}}')
    private String validAcrValues

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
                        response = UpdateServiceResult.builder().status(SKIPPED).build()
                    } else {
                        response = updateService.update(UpdateServiceInput.builder()
                                .code(request.code)
                                .relyingParty(request.clientId)
                                .data(request.data)
                                .build()).toBlocking().single()
                    }
                    new ImmutableTriple<UpdateServiceRequest, UpdateServiceResult, UpdateDataEntry>(request, response, data)
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
                LocalDateTime authCodeExpiresIn = now.plus(relyingParty.codeTTL, ChronoUnit.SECONDS)

                AcrValue receivedAcrValues = sessionService.getByValidSessionToken(sessionToken, fromLocal(now), false)
                        .map { session -> session.getReceivedAcrValues() }.toBlocking().single()

                if (!parseAcrValues().contains(receivedAcrValues.toString()) && validAcrValues != null) {
                    throw new AuthorizationException("Not valid acr_values")
                }

                sessionService.updateAuthCodeExpired(authCode, fromLocal(authCodeExpiresIn))
                        .filter { it.booleanValue() }
                        .switchIfEmpty(Observable.error(new AuthorizationException(AUTH_CODE_EXPIRES_IN_UPDATE_FAILED)))
                        .subscribe()

                long expiresIn = ChronoUnit.SECONDS.between(now, authCodeExpiresIn)
                //send redirect with code --resp.buildURL()
                def finishResponse = new UpdateFinishResponse(relyingParty.authorizationRedirectURI, sessionToken, authCode, expiresIn)
                ctx.redirect(HttpResponseStatus.FOUND.code(), finishResponse.redirectURL)
        } {
            throwable -> ctx.get(ServerErrorHandler).error(ctx, throwable)
        }
    }

    private List<String> parseAcrValues() {
        return Arrays.asList(validAcrValues.split(","));
    }
}

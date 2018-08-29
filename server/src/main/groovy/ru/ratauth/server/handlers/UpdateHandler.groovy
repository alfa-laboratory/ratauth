package ru.ratauth.server.handlers


import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import ratpack.form.Form
import ratpack.func.Action
import ratpack.handling.Chain
import ru.ratauth.server.services.AuthClientService
import ru.ratauth.server.updateServices.UpdateServiceResolver
import ru.ratauth.services.SessionService
import ru.ratauth.services.UpdateDataService

import static ratpack.rx.RxRatpack.observe
import static ru.ratauth.server.handlers.readers.UpdateServiceRequestReader.readUpdateServiceRequest

@Component
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
        chain.path('update') { context ->
            context.byMethod { method ->
                method.post { // POST
                    def queryParams = context.parse(Form)
                    def requestObs = observe(queryParams).map { res -> readUpdateServiceRequest(res, context.request.headers) }
//                    update(context, requestObs)
                }
            }
        }
    }

//    private void update(Context context, Observable<UpdateServiceRequest> requestObs) {
//        requestObs.flatMap {
//            request ->
//        }  subscribe( {
//            updateServiceResolver.getUpdateProvider(request.updateService)
//                    .update(UpdateServiceInput.builder()
//                    .relyingParty(request.clientId)
//                    .code(request.code)
//                    .data(request.data)
//                    .build())
//                    .filter { response -> (response.status == SUCCESS) }
//                    .map { r -> {
//                        def session = sessionService.getByValidSessionToken(updateDataService.getValidEntry(request.code).toBlocking().single().sessionId).toBlocking().single()
//                        def authCode = session.getEntry(request.clientId).get()
//                        sessionService.updateAuthCodeExpired(authCode, fromLocal(authCodeExpiresIn))
//                    }}
//                    .map { response -> new ImmutablePair<String, UpdateServiceOutput>(request.clientId, response)}
//                    subscribe ({
//            response ->
//                def clientId = response.left
//                authClientService.loadRelyingParty(clientId)
//                        .map { relyingParty -> context.redirect(HttpResponseStatus.FOUND.code(), relyingParty.authorizationRedirectURI)}
//        }, {
//            throwable -> context.get(ServerErrorHandler).error(context, throwable)
//        })
//    }
}

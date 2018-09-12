package ru.ratauth.server.updateServices

import groovy.json.JsonSlurper
import groovy.transform.CompileStatic
import lombok.SneakyThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import ratpack.http.client.ReceivedResponse
import ru.ratauth.exception.AuthorizationException
import ru.ratauth.exception.UpdateFlowException
import ru.ratauth.exception.UpdateFlowException.ID
import ru.ratauth.server.command.HystrixUpdateServiceCommand
import ru.ratauth.server.configuration.UpdateServicesConfiguration
import ru.ratauth.server.handlers.HttpClientHolder
import ru.ratauth.updateServices.UpdateService
import ru.ratauth.updateServices.dto.UpdateServiceInput
import ru.ratauth.updateServices.dto.UpdateServiceResult
import ru.ratauth.updateServices.dto.UpdateServiceResult.Status
import rx.Observable

@Component
@CompileStatic
class RestUpdateService implements UpdateService {

    @Autowired
    UpdateServicesConfiguration configuration;

    @Override
    @SneakyThrows
    Observable<UpdateServiceResult> update(UpdateServiceInput updateServiceInput) {

        def serviceConfiguration = configuration.updateServices[updateServiceInput.updateService]

        return new HystrixUpdateServiceCommand(
                HttpClientHolder.getInstance(),
                updateServiceInput.data,
                updateServiceInput.relyingParty,
                updateServiceInput.updateService,
                serviceConfiguration.uri,
                serviceConfiguration.authLogin,
                serviceConfiguration.authPassword,
                serviceConfiguration.readTimeout
        ).toObservable()
                .map({ ReceivedResponse res ->
            if (res.status.'4xx') {
                throw new AuthorizationException(res.getBody().getText())
            } else if (res.status.'5xx') {
                throw new UpdateFlowException(ID.UPDATE_CALL_SERVICE.name(), res.getBody().getText())
            }
            return makeUpdateResultFromResponse(res)
        })
    }

    private static UpdateServiceResult makeUpdateResultFromResponse(ReceivedResponse receivedResponse) {
        Map responseMap = (Map) (((List) (new JsonSlurper().parseText(receivedResponse.getBody().getText()))).get(0))
        return UpdateServiceResult.builder().status(Status.valueOf((String) responseMap.get("status")))
                .data(responseMap)
                .build()
    }
}

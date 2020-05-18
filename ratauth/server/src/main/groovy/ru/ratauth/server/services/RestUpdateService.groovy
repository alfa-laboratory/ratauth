package ru.ratauth.server.services

import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import lombok.SneakyThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import ratpack.http.client.ReceivedResponse
import ru.ratauth.exception.AuthorizationException
import ru.ratauth.exception.UpdateFlowException
import ru.ratauth.server.command.HystrixUpdateServiceCommand
import ru.ratauth.server.configuration.UpdateServicesConfiguration
import ru.ratauth.server.handlers.HttpClientHolder
import ru.ratauth.update.services.UpdateService
import ru.ratauth.update.services.dto.UpdateServiceInput
import ru.ratauth.update.services.dto.UpdateServiceResult
import rx.Observable

import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY
import static ru.ratauth.exception.UpdateFlowException.ID.UPDATE_CALL_SERVICE
import static ru.ratauth.update.services.dto.UpdateServiceResult.Status.ERROR

@Slf4j
@Component
class RestUpdateService implements UpdateService {

    @Autowired
    UpdateServicesConfiguration configuration

    @Override
    @SneakyThrows
    Observable<UpdateServiceResult> update(UpdateServiceInput updateServiceInput) {

        def serviceConfiguration = configuration.updateServices[updateServiceInput.updateService]

        log.info("Update request params: ${updateServiceInput.toString()} ")

        return new HystrixUpdateServiceCommand(
                HttpClientHolder.instance,
                updateServiceInput.data,
                updateServiceInput.relyingParty,
                updateServiceInput.updateService,
                serviceConfiguration.uri,
                serviceConfiguration.authLogin,
                serviceConfiguration.authPassword,
                serviceConfiguration.readTimeout
        ).toObservable()
                .map({ ReceivedResponse res ->
            if (res.status.code == UNPROCESSABLE_ENTITY.value()) {
                return makeUpdateResponseFromValidationError(res)
            } else if (res.status.'4xx') {
                throw new AuthorizationException(res.body.text)
            } else if (res.status.'5xx') {
                throw new UpdateFlowException(UPDATE_CALL_SERVICE.name(), res.body.text)
            }
            return makeUpdateResultFromResponse(res)
        })
    }

    private static UpdateServiceResult makeUpdateResponseFromValidationError(ReceivedResponse res) {
        return UpdateServiceResult.builder()
                .status(ERROR)
                .field("message", res.body.text)
                .build()
    }

    private static UpdateServiceResult makeUpdateResultFromResponse(ReceivedResponse receivedResponse) {
        def response = (new JsonSlurper().parseText(receivedResponse.body.text) as List)[0] as Map
        return new UpdateServiceResult(response.data as Map)
    }
}

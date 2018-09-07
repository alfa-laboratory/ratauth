package ru.ratauth.server.updateServices;

import groovy.json.JsonSlurper;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import ratpack.http.client.ReceivedResponse;
import ru.ratauth.exception.AuthorizationException;
import ru.ratauth.exception.UpdateFlowException;
import ru.ratauth.exception.UpdateFlowException.ID;
import ru.ratauth.server.command.HystrixUpdateServiceCommand;
import ru.ratauth.server.configuration.UpdateServiceConfiguration;
import ru.ratauth.server.configuration.UpdateServicesConfiguration;
import ru.ratauth.server.handlers.HttpClientHolder;
import ru.ratauth.updateServices.UpdateService;
import ru.ratauth.updateServices.dto.UpdateServiceInput;
import ru.ratauth.updateServices.dto.UpdateServiceResult;
import ru.ratauth.updateServices.dto.UpdateServiceResult.Status;
import rx.Observable;

@Component
@RequiredArgsConstructor
public class RestUpdateService implements UpdateService {

    private final UpdateServicesConfiguration configuration;

    @Override
    @SneakyThrows
    public Observable<UpdateServiceResult> update(UpdateServiceInput updateServiceInput) {
        UpdateServiceConfiguration serviceConfiguration = configuration.getUpdateService()
            .get(updateServiceInput.getUpdateService());

        return new HystrixUpdateServiceCommand(HttpClientHolder.getInstance(),
            updateServiceInput.getData(),
            updateServiceInput.getRelyingParty(),
            updateServiceInput.getUpdateService(),
            serviceConfiguration.getUrl(),
            serviceConfiguration.getAuthLogin(),
            serviceConfiguration.getAuthPassword(),
            serviceConfiguration.getReadTimeout()
        ).toObservable()
            .map(res -> {
                if (res.getStatus().is4xx()) {
                    throw new AuthorizationException(res.getBody().getText());
                } else if (res.getStatus().is5xx()) {
                    throw new UpdateFlowException(ID.UPDATE_CALL_SERVICE.name(), res.getBody().getText());
                }
                return makeUpdateResultFromResponse(res);
            });
    }

    private UpdateServiceResult makeUpdateResultFromResponse(ReceivedResponse receivedResponse) {
        Map responseMap = (Map) (((List) (new JsonSlurper().parseText(receivedResponse.getBody().getText()))).get(0));
        return UpdateServiceResult.builder().status(Status.valueOf((String) responseMap.get("status")))
            .data(responseMap)
            .build();
    }
}

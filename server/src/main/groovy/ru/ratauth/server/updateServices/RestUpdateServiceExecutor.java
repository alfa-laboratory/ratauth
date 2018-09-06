package ru.ratauth.server.updateServices;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ratpack.http.client.ReceivedResponse;
import ru.ratauth.server.command.HystrixUpdateServiceCommand;
import ru.ratauth.server.configuration.UpdateServiceConfiguration;
import ru.ratauth.server.configuration.UpdateServicesConfiguration;
import ru.ratauth.server.handlers.HttpClientHolder;
import ru.ratauth.updateServices.dto.UpdateServiceInput;
import ru.ratauth.updateServices.dto.UpdateServiceOutput;
import rx.Observable;

@Component
@RequiredArgsConstructor
public class RestUpdateServiceExecutor implements UpdateServiceExecutor {

    private final UpdateServicesConfiguration configuration;

    @Override
    public Observable<Object> getUpdateService(UpdateServiceInput updateServiceInput)
        throws MalformedURLException, URISyntaxException {
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
                //TODO make new exceptions
                if (res.getStatus().is4xx()) {
                    throw new RuntimeException(res.getBody().getText());
                } else if (res.getStatus().is5xx()) {
                    throw new RuntimeException(res.getBody().getText());
                }
                return makeUpdateResultFromResponse(res);
            });
    }

    private UpdateServiceOutput makeUpdateResultFromResponse(ReceivedResponse res) {

        return null;
    }
}

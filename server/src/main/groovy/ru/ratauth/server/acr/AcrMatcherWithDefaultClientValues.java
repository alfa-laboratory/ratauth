package ru.ratauth.server.acr;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import ratpack.http.Request;
import ru.ratauth.entities.AcrValues;
import ru.ratauth.server.handlers.readers.ReadRequestException;
import ru.ratauth.services.ClientService;

import java.util.Optional;

import static javaslang.Tuple.of;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AcrMatcherWithDefaultClientValues implements AcrMatcher {

    final private ClientService clientService;

    @Override
    public String match(Request request) {

        try {
            AcrValues defaultAcrValues = clientService
                    .getRelyingParty(request.getQueryParams().get("client_id"))
                    .toBlocking().single()
                    .getDefaultAcrValues();

            String acrValues = Optional
                    .ofNullable(request.getQueryParams().get("acr_values"))
                    .orElse(defaultAcrValues != null ? defaultAcrValues.toString() : null);

            return of(acrValues)
                    .map(AcrValues::valueOf)
                    .map(AcrValues::getValues)
                    .map(r -> r.get(0))
                    ._1();

        } catch (Exception ex) {
            throw new ReadRequestException("acr values must not be null");
        }
    }
}
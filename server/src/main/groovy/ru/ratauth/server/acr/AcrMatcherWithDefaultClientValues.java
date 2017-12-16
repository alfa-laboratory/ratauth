package ru.ratauth.server.acr;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import ratpack.http.Request;
import ru.ratauth.entities.AcrValues;
import ru.ratauth.entities.RelyingParty;
import ru.ratauth.server.handlers.readers.ReadRequestException;
import ru.ratauth.services.ClientService;
import rx.Observable;

import java.util.Objects;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AcrMatcherWithDefaultClientValues implements AcrMatcher {

    final private ClientService clientService;

    @Override
    public String match(Request request) {

        try {
            return Observable.just(request.getQueryParams().get("acr_values"))
                    .filter(Objects::nonNull)
                    .map(AcrValues::valueOf)
                    .map(AcrValues::getValues)
                    .map(acr -> acr.get(0))
                    .switchIfEmpty(fetchDefaultACR(request))
                    .toBlocking()
                    .single();

        } catch (Exception ex) {
            throw new ReadRequestException("acr values must not be null");
        }
    }

    private Observable<? extends String> fetchDefaultACR(Request request) {
        return clientService
                .getRelyingParty(request.getQueryParams().get("client_id"))
                .map(RelyingParty::getDefaultAcrValues)
                .map(Object::toString);
    }
}
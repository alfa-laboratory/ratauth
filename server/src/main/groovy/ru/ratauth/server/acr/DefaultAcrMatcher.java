package ru.ratauth.server.acr;

import ratpack.http.Request;
import ru.ratauth.entities.AcrValues;

import java.util.Optional;

public class DefaultAcrMatcher implements AcrMatcher {

    @Override
    public String match(Request request) {
        return Optional.of(request.getQueryParams().get("acr_values"))
                .map(AcrValues::valueOf)
                .map(AcrValues::getValues)
                .map(acrValues -> acrValues.get(0))
                .get();
    }
}

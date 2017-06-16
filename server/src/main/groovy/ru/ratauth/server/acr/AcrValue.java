package ru.ratauth.server.acr;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

import java.util.List;

import static java.util.Arrays.asList;

@Builder
public class AcrValue {

    @Getter
    @Singular("acr")
    private final List<String> acrValues;

    public static AcrValue valueOf(String acrValue) {
        return AcrValue.builder()
                .acrValues(asList(acrValue.split(":")))
                .build();
    }

    @Override
    public String toString() {
        return String.join(":", acrValues);
    }

}

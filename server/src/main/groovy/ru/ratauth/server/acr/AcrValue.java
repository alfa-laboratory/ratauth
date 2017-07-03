package ru.ratauth.server.acr;

import lombok.*;
import lombok.experimental.Wither;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

@Builder
@RequiredArgsConstructor
@EqualsAndHashCode
public class AcrValue {

    @Getter
    @Wither
    @NonNull
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

    public AcrValue difference(AcrValue acrValue) {
        return this.withAcrValues(this.getAcrValues().stream()
                .filter(value -> !acrValue.getAcrValues().contains(value))
                .collect(toList()));
    }

}

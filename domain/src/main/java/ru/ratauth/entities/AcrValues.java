package ru.ratauth.entities;

import lombok.*;
import lombok.experimental.Wither;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.*;
import static java.util.stream.Collectors.toList;

@Builder
@RequiredArgsConstructor
@EqualsAndHashCode
public class AcrValues implements AcrValue, Enroll {

    @Getter
    @Wither
    @NonNull
    @Singular("acr")
    private final List<String> acrValues;

    public static AcrValues valueOf(String acrValue) {
        return AcrValues.builder()
                .acrValues(unmodifiableList(asList(acrValue.split(":"))))
                .build();
    }

    public String getFirst() {
        return this.getAcrValues().get(0);
    }

    public AcrValues difference(AcrValues acrValues) {
        return this.withAcrValues(this.getAcrValues().stream()
                .filter(value -> !acrValues.getAcrValues().contains(value))
                .collect(toList()));
    }

    @Override
    public String toString() {
        return String.join(":", acrValues);
    }
    
    @Override
    public Iterator<String> iterator() {
        return acrValues.iterator();
    }
}

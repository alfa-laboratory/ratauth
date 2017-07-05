package ru.ratauth.entities;

import lombok.*;
import lombok.experimental.Wither;

import javax.annotation.Nonnull;
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
    private final List<String> values;

    public static AcrValues valueOf(String acrValue) {
        return AcrValues.builder()
                .values(unmodifiableList(asList(acrValue.split(":"))))
                .build();
    }

    public String getFirst() {
        return this.getValues().get(0);
    }

    public AcrValues difference(AcrValues acrValues) {
        return this.withValues(this.getValues().stream()
                .filter(value -> !acrValues.getValues().contains(value))
                .collect(toList()));
    }

    @Override
    public String toString() {
        return String.join(":", values);
    }
    
    @Nonnull
    @Override
    public Iterator<String> iterator() {
        return values.iterator();
    }
}

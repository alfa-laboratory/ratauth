package ru.ratauth.server.scope;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

import java.util.Arrays;
import java.util.List;

import static java.util.Arrays.asList;

@Builder
public class Scope {

    @Singular
    @Getter
    private final List<String> scopes;

    public static Scope valueOf(String scope) {
        return Scope.builder()
                .scopes(asList(scope.split(":")))
                .build();
    }

    @Override
    public String toString() {
        return String.join(":", scopes);
    }

    public static Scope fromString(String scope) {
        return Scope.builder().scopes(Arrays.asList(scope.split(":"))).build();
    }
}

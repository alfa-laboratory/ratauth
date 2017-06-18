package ru.ratauth.server.scope;

import lombok.Builder;
import lombok.Singular;

import java.util.Arrays;
import java.util.List;

@Builder
public class Scope {

    @Singular
    private final List<String> scopes;

    @Override
    public String toString() {
        return String.join(":", scopes);
    }

    public static Scope fromString(String scope) {
        return Scope.builder().scopes(Arrays.asList(scope.split(":"))).build();
    }
}

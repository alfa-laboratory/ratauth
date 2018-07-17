package ru.ratauth.providers.auth;

import lombok.extern.slf4j.Slf4j;
import ru.ratauth.exception.MissingProviderException;

import java.util.List;
import java.util.Map;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;

public class VerifierResolver {

    private final Map<String, Verifier> verifiers;

    public VerifierResolver(List<Verifier> verifiers) {
        this.verifiers = verifiers.stream().collect(toMap(Verifier::name, v -> v));
    }

    public Verifier find(String enroll) {
        return ofNullable(verifiers.get(enroll))
                .orElseThrow(() -> new MissingProviderException(enroll));
    }

}
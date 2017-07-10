package ru.ratauth.server.providers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ratauth.providers.auth.Verifier;
import ru.ratauth.server.extended.enroll.MissingProviderException;

import java.util.List;
import java.util.Map;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;

@Component
public class VerifierResolver {

    private final Map<String, Verifier> verifiers;

    @Autowired
    public VerifierResolver(List<Verifier> verifiers) {
        this.verifiers = verifiers.stream().collect(toMap(Verifier::name, v -> v));
    }

    public Verifier find(String enroll) {
        return ofNullable(verifiers.get(enroll))
                .orElseThrow(() -> new MissingProviderException(enroll));
    }

}
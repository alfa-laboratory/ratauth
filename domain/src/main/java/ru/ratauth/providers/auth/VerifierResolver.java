package ru.ratauth.providers.auth;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ratauth.exception.MissingProviderException;

import java.util.List;
import java.util.Map;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;

@Slf4j
@Component
public class VerifierResolver {

    private final Map<String, Verifier> verifiers;

    @Autowired
    public VerifierResolver(List<Verifier> verifiers) {
        this.verifiers = verifiers.stream().collect(toMap(Verifier::name, v -> v));
        log.info("Loading verifiers: " + this.verifiers.keySet());
    }

    public Verifier find(String enroll) {
        return ofNullable(verifiers.get(enroll))
                .orElseThrow(() -> new MissingProviderException(enroll));
    }

}
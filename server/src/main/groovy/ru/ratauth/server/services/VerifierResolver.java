package ru.ratauth.server.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import ru.ratauth.providers.auth.Verifier;
import ru.ratauth.server.extended.enroll.MissingProviderException;

import java.util.Map;

import static java.util.Optional.ofNullable;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class VerifierResolver {

    private final Map<String, Verifier> providers;


    public Verifier find(String clientId) {
        return ofNullable(providers.get(clientId.concat("IdentityProvider")))
                .orElseThrow(() -> new MissingProviderException(clientId.concat("IdentityProvider")));
    }

}
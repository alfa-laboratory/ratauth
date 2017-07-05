package ru.ratauth.server.providers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ratauth.providers.auth.Verifier;
import ru.ratauth.server.extended.enroll.MissingProviderException;

import java.util.Map;

import static java.util.Optional.ofNullable;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class VerifierResolver {

    private final Map<String, Verifier> providers;

    public Verifier find(String enroll) {
        return ofNullable(providers.get(enroll.concat("IdentityProvider")))
                .orElseThrow(() -> new MissingProviderException(enroll.concat("IdentityProvider")));
    }

}
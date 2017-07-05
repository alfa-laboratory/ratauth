package ru.ratauth.server.providers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ratauth.entities.IdentityProvider;
import ru.ratauth.server.extended.enroll.MissingProviderException;

import java.util.List;
import java.util.Map;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;

@Component
public class IdentityProviderResolverImpl implements IdentityProviderResolver {

    private final Map<String, IdentityProvider> identityProviders;

    @Autowired
    public IdentityProviderResolverImpl(List<IdentityProvider> identityProviders) {
        this.identityProviders = identityProviders.stream().collect(toMap(IdentityProvider::name, v -> v));
    }


    @Override
    public IdentityProvider getProvider(String identityProviderName) {
        return ofNullable(identityProviders.get(identityProviderName))
                .orElseThrow(() -> new MissingProviderException(identityProviderName));
    }
}

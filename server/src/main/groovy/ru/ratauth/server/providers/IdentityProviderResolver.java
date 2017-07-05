package ru.ratauth.server.providers;

import ru.ratauth.entities.IdentityProvider;

public interface IdentityProviderResolver {

    IdentityProvider getProvider(String clientId);

}

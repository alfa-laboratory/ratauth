package ru.ratauth.inmemory.ip.providers;

import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import ru.ratauth.providers.auth.AuthProvider;
import ru.ratauth.providers.registrations.RegistrationProvider;

@RequiredArgsConstructor
public class InMemoryAuthRegistrationProvider implements AuthProvider, RegistrationProvider {

    @Delegate
    private final AuthProvider authProvider;

    @Delegate
    private final RegistrationProvider registrationProvider;

}
package ru.ratauth.server.configuration;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class IdentityProviderConfiguration {
    DestinationConfiguration activate;
    DestinationConfiguration verify;
    DestinationConfiguration restrictions;
}

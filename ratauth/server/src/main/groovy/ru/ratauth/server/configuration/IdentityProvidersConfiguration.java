package ru.ratauth.server.configuration;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@Data
@NoArgsConstructor
@ConfigurationProperties("ratauth")
public class IdentityProvidersConfiguration {
    private Map<String, IdentityProviderConfiguration> idp;
    private Integer timeout;
}

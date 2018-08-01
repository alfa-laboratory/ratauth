package ru.ratauth.server.configuration;

import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@Data
@NoArgsConstructor
@ConfigurationProperties("ratauth")
public class IdentityProvidersConfiguration {
    private Map<String, IdentityProviderConfiguration> idp;
    @NotNull
    private Integer timeout;
}

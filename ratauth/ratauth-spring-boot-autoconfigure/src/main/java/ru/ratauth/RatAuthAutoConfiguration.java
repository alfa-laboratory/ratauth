package ru.ratauth;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import ratpack.spring.config.EnableRatpack;
import ru.ratauth.server.configuration.IdentityProvidersConfiguration;
import ru.ratauth.server.configuration.OpenIdConnectDefaultDiscoveryProperties;
import ru.ratauth.server.configuration.UpdateServicesConfiguration;

@Configuration
@EnableRatpack
@ComponentScan("ru.ratauth.server")
@EnableConfigurationProperties({
        OpenIdConnectDefaultDiscoveryProperties.class,
        IdentityProvidersConfiguration.class,
        UpdateServicesConfiguration.class
})
public class RatAuthAutoConfiguration {

}

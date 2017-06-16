package ru.ratauth.server.authorize;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import ratpack.spring.config.EnableRatpack;
import ru.ratauth.server.authcode.AuthCodeServiceTestConfiguration;
import ru.ratauth.server.configuration.OpenIdConnectDiscoveryProperties;

@EnableRatpack
@Configuration
@EnableConfigurationProperties(OpenIdConnectDiscoveryProperties.class)
@Import(AuthCodeServiceTestConfiguration.class)
public class AuthorizeHandlerTestConfiguration {

    @Bean
    public AuthorizeHandler authorizeHandler(OpenIdConnectDiscoveryProperties openIdConnectDiscoveryProperties) {
        return new AuthorizeHandler(openIdConnectDiscoveryProperties);
    }

}

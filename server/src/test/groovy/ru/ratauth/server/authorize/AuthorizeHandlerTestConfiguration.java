package ru.ratauth.server.authorize;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import ratpack.spring.config.EnableRatpack;
import ru.ratauth.server.acr.AcrResolver;
import ru.ratauth.server.acr.AcrResolverConfiguration;
import ru.ratauth.server.authcode.AuthCodeServiceTestConfiguration;
import ru.ratauth.server.configuration.OpenIdConnectDiscoveryProperties;
import ru.ratauth.server.jwt.JWTDecoder;

@EnableRatpack
@Configuration
@EnableConfigurationProperties(OpenIdConnectDiscoveryProperties.class)
@Import({AuthCodeServiceTestConfiguration.class, AcrResolverConfiguration.class})
public class AuthorizeHandlerTestConfiguration {

    @Bean
    public AuthorizeHandlerValidator authorizeHandlerValidator(JWTDecoder jwtDecoder) {
        return new AuthorizeHandlerValidator(jwtDecoder);
    }

    @Bean
    public AuthorizeHandler authorizeHandler(OpenIdConnectDiscoveryProperties openIdConnectDiscoveryProperties, AcrResolver acrResolver, AuthorizeHandlerValidator validator) {
        return new AuthorizeHandler(openIdConnectDiscoveryProperties, acrResolver, validator);
    }

}

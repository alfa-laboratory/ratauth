package ru.ratauth.server

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.context.properties.EnableConfigurationProperties
import ratpack.spring.config.EnableRatpack
import ru.ratauth.server.configuration.IdentityProvidersConfiguration
import ru.ratauth.server.configuration.OpenIdConnectDefaultDiscoveryProperties
import ru.ratauth.server.configuration.SessionConfiguration
import ru.ratauth.server.configuration.UpdateServicesConfiguration


@Slf4j
@CompileStatic
@SpringBootApplication
@EnableRatpack
@EnableConfigurationProperties([
        OpenIdConnectDefaultDiscoveryProperties,
        IdentityProvidersConfiguration,
        SessionConfiguration,
        UpdateServicesConfiguration
])
class RatAuthApplication {
    public static final int DEFAULT_PADDING = 50

    static void main(String[] args) {
        log.debug 'Starting'.center(DEFAULT_PADDING, '=')

        new SpringApplicationBuilder(RatAuthApplication)
                .web(false)
                .run(args)

        log.debug 'Started'.center(DEFAULT_PADDING, '=')
    }
}
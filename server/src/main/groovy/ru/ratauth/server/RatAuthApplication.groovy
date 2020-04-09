package ru.ratauth.server

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.boot.WebApplicationType
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.context.properties.EnableConfigurationProperties
import ratpack.spring.config.EnableRatpack
import ru.ratauth.server.configuration.*

@Slf4j
@CompileStatic
@SpringBootApplication
@EnableRatpack
@EnableConfigurationProperties([
        OpenIdConnectDefaultDiscoveryProperties,
        IdentityProvidersConfiguration,
        SessionConfiguration,
        UpdateServicesConfiguration,
        RestrictionServiceConfiguration
])
class RatAuthApplication {
    public static final int DEFAULT_PADDING = 50

    static void main(String[] args) {
        log.debug 'Starting'.center(DEFAULT_PADDING, '=')

        new SpringApplicationBuilder(RatAuthApplication)
                .web(WebApplicationType.NONE)
                .run(args)

        log.debug 'Started'.center(DEFAULT_PADDING, '=')
    }
}
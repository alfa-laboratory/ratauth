package ru.ratauth.server

import groovy.util.logging.Slf4j
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import ratpack.spring.config.EnableRatpack
import ru.ratauth.server.authcode.AuthCodeProperties
import ru.ratauth.server.authcode.AuthCodeService
import ru.ratauth.server.configuration.OpenIdConnectDiscoveryProperties
import ru.ratauth.server.jwt.JWTProperties
import ru.ratauth.server.scope.ScopeProperties

@Slf4j
@SpringBootApplication
@EnableRatpack
@EnableConfigurationProperties([OpenIdConnectDiscoveryProperties, AuthCodeProperties, ScopeProperties, JWTProperties])
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

package ru.ratauth.server

import groovy.util.logging.Slf4j
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Import
import ratpack.spring.config.EnableRatpack
import ru.ratauth.configuration.BaseConfiguration
import ru.ratauth.configuration.DCAConfiguration
import ru.ratauth.server.configuration.OpenIdConnectDiscoveryProperties
import ru.ratauth.server.configuration.RatAuthProperties

@Slf4j
@SpringBootApplication
@EnableRatpack
@EnableConfigurationProperties([RatAuthProperties, OpenIdConnectDiscoveryProperties])
@Import([DCAConfiguration, BaseConfiguration])
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
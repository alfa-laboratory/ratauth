package ru.ratauth;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import ratpack.spring.config.EnableRatpack;
import ru.ratauth.server.configuration.OpenIdConnectDiscoveryProperties;

@Configuration
@EnableRatpack
@ComponentScan("ru.ratauth.server")
@EnableConfigurationProperties({OpenIdConnectDiscoveryProperties.class})
public class RatAuthAutoConfiguration {

}

package ru.ratauth.server;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.actuate.autoconfigure.EndpointAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import ratpack.spring.config.EnableRatpack;
import ru.ratauth.server.configuration.OpenIdConnectDiscoveryProperties;

@SpringBootConfiguration
@EnableRatpack
@ComponentScan
@EnableConfigurationProperties(OpenIdConnectDiscoveryProperties.class)
@Import({JacksonAutoConfiguration.class, EndpointAutoConfiguration.class})
public class RatAuthAutoConfiguration {

}

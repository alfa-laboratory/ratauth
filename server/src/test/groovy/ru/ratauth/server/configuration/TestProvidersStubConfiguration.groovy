package ru.ratauth.server.configuration

import groovy.transform.CompileStatic
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Configuration
import ratpack.spring.config.EnableRatpack
import ru.ratauth.server.local.ProvidersStubConfiguration
/**
 * @author mgorelikov
 * @since 03/11/15
 */
@EnableRatpack
@Configuration
@SpringBootApplication
@CompileStatic
class TestProvidersStubConfiguration extends ProvidersStubConfiguration {

}

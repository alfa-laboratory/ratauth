package ru.ratauth.server.configuration;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

/**
 * @author tolkv
 * @version 23/09/16
 */
@TestConfiguration
@Import({TestProvidersStubConfiguration.class, TestPersistenceServiceStubConfiguration.class})
@TestPropertySource(properties = {
        "ratpack.port=8080",
        "ratpack.base-dir=file:server/src/main/resources",
        "ratpack.templates-path=templates"
})
public class TestBaseConfiguration {
}

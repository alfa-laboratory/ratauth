package ru.ratauth.server.configuration;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

/**
 * @author tolkv
 * @version 23/09/16
 */
@TestConfiguration
@Import({TestProvidersStubConfiguration.class, TestPersistenceServiceStubConfiguration.class})
public class TestBaseConfiguration {
}

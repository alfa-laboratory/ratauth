package ru.ratauth.server.configuration.annotation;

import org.springframework.context.annotation.Profile;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author mgorelikov
 * @since 12/09/16
 */
@Retention(RetentionPolicy.RUNTIME)
@Profile("cloud")
public @interface Cloud {
}

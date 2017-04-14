package ru.ratauth.server.local;

import org.springframework.context.annotation.Profile;

import java.lang.annotation.*;

/**
 * @author tolkv
 * @version 21/09/16
 */
@Profile("local")
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface LocalProfile {
}

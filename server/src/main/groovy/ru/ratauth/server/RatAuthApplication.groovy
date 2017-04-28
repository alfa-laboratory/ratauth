package ru.ratauth.server

import groovy.util.logging.Slf4j
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder

@Slf4j
@SpringBootApplication
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

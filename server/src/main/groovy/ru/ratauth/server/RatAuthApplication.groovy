package ru.ratauth.server

import groovy.util.logging.Slf4j
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@Slf4j
@SpringBootApplication
public class RatAuthApplication {
  public static final int DEFAULT_PADDING = 50

  public static void main(String[] args) {
    log.debug 'Starting'.center(DEFAULT_PADDING, '=')
    SpringApplication.run RatAuthApplication, args
    log.debug 'Started'.center(DEFAULT_PADDING, '=')
  }



}
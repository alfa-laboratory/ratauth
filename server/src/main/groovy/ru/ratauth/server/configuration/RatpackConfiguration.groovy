package ru.ratauth.server.configuration
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.FileSystemResource

import javax.annotation.PostConstruct

/**
 * @author tolkv
 * @since 30/10/15
 */
@Configuration
class RatpackConfiguration {

  Logger log = LoggerFactory.getLogger(RatpackConfiguration)

  @Autowired
  ratpack.spring.config.RatpackProperties properties

  //TODO temporary fix for ratpack error. Ratpack application could not be executed from jar
  @PostConstruct
  void fixBaseDir() {
    properties.basedir =  new FileSystemResource("src/main/resources");
  }

}

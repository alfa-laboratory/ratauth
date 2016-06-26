package ru.ratauth.server

import com.jayway.restassured.builder.RequestSpecBuilder
import com.jayway.restassured.specification.RequestSpecification
import org.junit.Rule
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.IntegrationTest
import org.springframework.boot.test.SpringApplicationConfiguration
import org.springframework.restdocs.JUnitRestDocumentation
import org.springframework.test.context.TestPropertySource
import ratpack.test.ApplicationUnderTest
import spock.lang.Shared
import spock.lang.Specification

import static org.springframework.restdocs.restassured.RestAssuredRestDocumentation.documentationConfiguration

/**
 * @author mgorelikov
 * @since 26/06/16
 */
@SpringApplicationConfiguration(classes = RatAuthApplication.class)
@IntegrationTest(['server.port=8080', 'management.port=0'])
@TestPropertySource(locations = "classpath:application.yml")
class BaseDocumentationSpec extends Specification {
  @Shared
  ApplicationUnderTest aut = new RatAuthServerUnderTest()

  @Rule
  JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation('../docs/src/docs/generated-snippets/api')

  protected RequestSpecification documentationSpec

  void setup() {
    this.documentationSpec = new RequestSpecBuilder()
        .addFilter(documentationConfiguration(restDocumentation))
        .build()
  }
}

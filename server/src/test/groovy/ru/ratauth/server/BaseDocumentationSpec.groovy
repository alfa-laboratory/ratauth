package ru.ratauth.server

import com.jayway.restassured.builder.RequestSpecBuilder
import com.jayway.restassured.config.RestAssuredConfig
import com.jayway.restassured.specification.RequestSpecification
import org.junit.Rule
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.restdocs.JUnitRestDocumentation
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import ru.ratauth.server.configuration.TestBaseConfiguration
import spock.lang.Specification

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE
import static org.springframework.restdocs.restassured.RestAssuredRestDocumentation.documentationConfiguration

/**
 * @author mgorelikov
 * @since 26/06/16
 */
@SpringBootTest(
    webEnvironment = NONE,
    classes = [TestBaseConfiguration],
    properties = "ratpack.port=8080"
)
@TestPropertySource(locations = "classpath:application.yml")
@ActiveProfiles("test")
class BaseDocumentationSpec extends Specification {
  @Rule
  JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation('../server/build/docs/generated-snippets/api')

  protected RequestSpecification documentationSpec

  void setup() {
    this.documentationSpec = new RequestSpecBuilder()
        .addFilter(documentationConfiguration(restDocumentation))
        .setConfig(RestAssuredConfig.config().redirect(RestAssuredConfig.config().getRedirectConfig().followRedirects(false)))
        .build()
  }
}

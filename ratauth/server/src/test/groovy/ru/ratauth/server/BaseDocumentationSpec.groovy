package ru.ratauth.server

import io.restassured.builder.RequestSpecBuilder
import io.restassured.config.RestAssuredConfig
import io.restassured.specification.RequestSpecification
import org.junit.Rule
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.restdocs.JUnitRestDocumentation
import ru.ratauth.server.configuration.TestBaseConfiguration
import spock.lang.Specification

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE
import static org.springframework.restdocs.restassured3.RestAssuredRestDocumentation.documentationConfiguration

/**
 * @author mgorelikov
 * @since 26/06/16
 */
@SpringBootTest(
        webEnvironment = NONE,
        classes = [TestBaseConfiguration],
        properties = [
                "ratpack.port=8080"
        ]
)
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

package ru.ratauth.server.handlers

import com.jayway.restassured.http.ContentType
import ru.ratauth.server.BaseDocumentationSpec

import static com.jayway.restassured.RestAssured.given
import static org.springframework.http.HttpStatus.OK

class ActivatorsVerifiersVersionHandlerTest extends BaseDocumentationSpec {

    def 'should return openid discovery fields'() {
        given:
        def setup = given(this.documentationSpec)
                .accept(ContentType.JSON)
        when:
        def result = setup.when().get("providers/username/verifier/version")
        then:
        result.then()
                .statusCode(OK.value())
    }

}

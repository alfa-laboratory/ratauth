package ru.ratauth.server.handlers

import io.restassured.http.ContentType
import ru.ratauth.server.BaseDocumentationSpec

import static io.restassured.RestAssured.given
import static org.springframework.http.HttpStatus.OK

class ActivatorsVerifiersVersionHandlerTest extends BaseDocumentationSpec {

    //TODO что нибудь сделать с этим
//    def 'should return openid discovery fields'() {
//        given:
//        def setup = given(this.documentationSpec)
//                .accept(ContentType.JSON)
//        when:
//        def result = setup.when().get("providers/username/verifier/version")
//        then:
//        result.then()
//                .statusCode(OK.value())
//    }

}

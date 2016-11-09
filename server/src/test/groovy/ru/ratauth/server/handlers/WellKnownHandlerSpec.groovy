package ru.ratauth.server.handlers

import com.jayway.restassured.http.ContentType
import org.springframework.restdocs.payload.JsonFieldType
import ru.ratauth.server.BaseDocumentationSpec

import static com.jayway.restassured.RestAssured.given
import static org.springframework.http.HttpStatus.OK
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import static org.springframework.restdocs.restassured.RestAssuredRestDocumentation.document
/**
 * @author tolkv
 * @version 09/11/2016
 */
class WellKnownHandlerSpec extends BaseDocumentationSpec {
  def 'should return openid discovery fields'() {
    given:
    def setup = given(this.documentationSpec)
        .accept(ContentType.JSON)
        .filter(document('should_return_openid_discovery_fields',
        preprocessResponse(prettyPrint()),
        responseFields(
            fieldWithPath('issuer')
                .description('JWT token')
                .type(JsonFieldType.STRING),
            fieldWithPath('authorization_endpoint')
                .description('JWT token')
                .type(JsonFieldType.STRING),
            fieldWithPath('token_endpoint')
                .description('JWT token')
                .type(JsonFieldType.STRING),
            fieldWithPath('token_endpoint_auth_signing_alg_values_supported')
                .description('JWT token')
                .type(JsonFieldType.ARRAY),
            fieldWithPath('registration_endpoint')
                .description('JWT token')
                .type(JsonFieldType.STRING),
            fieldWithPath('userinfo_endpoint')
                .description('JWT token')
                .type(JsonFieldType.STRING),
            fieldWithPath('check_session_iframe')
                .description('JWT token')
                .type(JsonFieldType.STRING),
            fieldWithPath('end_session_endpoint')
                .description('JWT token')
                .type(JsonFieldType.STRING),
        )
    ))
    when:
    def result = setup.when().post('.well-known/openid-configuration')

    then:
    result.then()
        .statusCode(OK.value())
  }
}

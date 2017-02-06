package ru.ratauth.server

import com.fasterxml.jackson.databind.ObjectMapper
import com.jayway.restassured.http.ContentType
import org.hamcrest.core.StringContains
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.restdocs.payload.JsonFieldType
import ru.ratauth.server.local.PersistenceServiceStubConfiguration

import static com.jayway.restassured.RestAssured.given
import static groovy.json.JsonOutput.toJson
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders
import static org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint
import static org.springframework.restdocs.payload.PayloadDocumentation.*
import static org.springframework.restdocs.restassured.RestAssuredRestDocumentation.document
/**
 * @author mgorelikov
 * @since 02/02/17
 */
class FactorsAPISpec extends BaseDocumentationSpec {
  @Value('${server.port}')
  String port
  @Autowired
  ObjectMapper objectMapper

  def 'should enroll factor'() {
    given:
    def setup = given(this.documentationSpec)
        .accept(ContentType.JSON)
        .filter(document('enroll_factor_succeed',
        preprocessResponse(prettyPrint()),
        requestFields(
            fieldWithPath('access_token')
                .description('access token for session that must be enrolled')
                .type(JsonFieldType.STRING),
            fieldWithPath('acr_values')
                .description('required acr values for session after mfa')
                .type(JsonFieldType.ARRAY)
        ),
        requestHeaders(
            headerWithName(HttpHeaders.AUTHORIZATION)
                .description('Authorization header for relying party basic authorization')
        ),
        responseFields(
            fieldWithPath('enrollment_id')
                .description('enrollment identifier')
                .type(JsonFieldType.STRING),
            fieldWithPath('status')
                .description('enrollment status')
                .type(JsonFieldType.STRING),
            fieldWithPath('enrollment_url')
                .description('redirect url for enrollment process')
                .type(JsonFieldType.STRING)
        )
    ))
        .given()
        .body(toJson([
        'access_token': '16080bf6-dbe4-428e-b648-06739b59e920',
        'acr_values': ['3','4'] as List ] as Map))
        .header(IntegrationSpecUtil.createAuthHeaders(PersistenceServiceStubConfiguration.CLIENT_NAME,
        PersistenceServiceStubConfiguration.PASSWORD))
    when:
    def result = setup
        .when()
        .post("factor/enroll")
    then:
    result
        .then()
        .statusCode(HttpStatus.OK.value())
  }

  def 'should return factors list'() {
    given:
    def setup = given(this.documentationSpec)
        .filter(document('list_factor_succeed',
        preprocessResponse(prettyPrint()),
        responseFields(
            fieldWithPath('factors')
                .description('factors array')
                .type(JsonFieldType.ARRAY),
            fieldWithPath('factors[].factor_type')
                .description('factor type')
                .type(JsonFieldType.STRING),
            fieldWithPath('factors[].provider')
                .description('provider name')
                .type(JsonFieldType.STRING),
            fieldWithPath('factors[].activation_url')
                .description('activation url for factor')
                .type(JsonFieldType.STRING)
        )
    ))
        .given()
    when:
    def result = setup
        .when()
        .get("factor/16080bf6-dbe4-428e-b648-06739b59e920/list")
    then:
    result
        .then()
        .statusCode(HttpStatus.OK.value())
  }

  def 'should activate factor'() {
    given:
    def setup = given(this.documentationSpec)
        .accept(ContentType.JSON)
        .filter(document('activate_factor_succeed',
        preprocessResponse(prettyPrint()),
        requestFields(
            fieldWithPath('factor_type')
                .description('factor type that must be activated')
                .type(JsonFieldType.STRING),
            fieldWithPath('provider')
                .description('provider name that can activate factor')
                .type(JsonFieldType.STRING),
        ),
        responseFields(
            fieldWithPath('required_fields')
                .description('required fields  key:value array with field name and type')
                .type(JsonFieldType.ARRAY),
            fieldWithPath('required_fields[].name')
                .description('for example sms code')
                .type(JsonFieldType.STRING)
                .optional(),
            fieldWithPath('required_fields[].type')
                .description('field type e.g. String')
                .type(JsonFieldType.STRING)
                .optional(),
            fieldWithPath('required_fields[].length')
                .description('length of field')
                .type(JsonFieldType.STRING)
                .optional()
        )
    ))
        .given()
        .body(toJson([
        'factor_type': 'SMS',
        'provider': 'provider' ] as Map))
    when:
    def result = setup
        .when()
        .post("factor/16080bf6-dbe4-428e-b648-06739b59e920/activate")
    then:
    result
        .then()
        .statusCode(HttpStatus.OK.value())
  }

  def 'should verify factor'() {
    given:
    def setup = given(this.documentationSpec)
        .accept(ContentType.JSON)
        .filter(document('verify_factor_succeed',
        preprocessResponse(prettyPrint()),
        requestFields(
            fieldWithPath('code')
                .description('one of the required field for factor verification')
                .type(JsonFieldType.STRING)
                .optional()
        ),
        responseHeaders(
            headerWithName(HttpHeaders.LOCATION)
                .description('Header that contains redirect url for passing enrollment result to relying party that request it'))
    ))
        .given()
        .body(toJson(['code': '123456' ] as Map))
    when:
    def result = setup
        .when()
        .post("factor/16080bf6-dbe4-428e-b648-06739b59e920/verify")
    then:
    result
        .then()
        .statusCode(HttpStatus.FOUND.value())
        .header(HttpHeaders.LOCATION, StringContains.containsString("id_token="))
  }

}

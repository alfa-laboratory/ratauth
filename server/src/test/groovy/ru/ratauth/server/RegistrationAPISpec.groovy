package ru.ratauth.server

import com.fasterxml.jackson.databind.ObjectMapper
import com.jayway.restassured.http.ContentType
import org.hamcrest.core.StringContains
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.restdocs.payload.JsonFieldType
import ru.ratauth.interaction.AuthzResponseType
import ru.ratauth.interaction.GrantType
import ru.ratauth.server.local.PersistenceServiceStubConfiguration
import ru.ratauth.server.local.ProvidersStubConfiguration

import static com.jayway.restassured.RestAssured.given
import static org.hamcrest.Matchers.containsString
import static org.hamcrest.Matchers.isEmptyOrNullString
import static org.hamcrest.core.IsNot.not
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders
import static org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters
import static org.springframework.restdocs.restassured.RestAssuredRestDocumentation.document

/**
 * @author djassan
 * @since 11/09/16
 */
class RegistrationAPISpec extends BaseDocumentationSpec {
  @Value('${server.port}')
  String port
  @Autowired
  ObjectMapper objectMapper

  def 'should request registration code over provider channel'() {
    given:
    def setup = given(this.documentationSpec)
        .accept(ContentType.URLENC)
        .filter(document('reg_code_provider_channel_succeed',
        requestParameters(
            parameterWithName('client_id')
                .description('relying party identifier'),
            parameterWithName('scope')
                .description('Scope for authorization that will be provided through JWT to all resource servers in flow'),
            parameterWithName('response_type')
                .description('Response type that must be provided CODE or TOKEN'),
            parameterWithName(ProvidersStubConfiguration.REG_CREDENTIAL)
                .description('Some credential fields...')
                .optional()
        ),
        responseHeaders(
            headerWithName(HttpHeaders.LOCATION)
                .description('Header that contains authorization code for the next step of authorization code flow,' +
                '\nits expiration date and optional user identifier')
        )))
        .given()
        .queryParam('response_type', AuthzResponseType.CODE.name())
        .queryParam('client_id', PersistenceServiceStubConfiguration.CLIENT_NAME)
        .queryParam('scope', 'rs.read')
        .queryParam(ProvidersStubConfiguration.REG_CREDENTIAL, 'credential')
    when:
    def result = setup
        .when()
        .get("register")
    then:
    result
        .then()
        .statusCode(HttpStatus.FOUND.value())
        .header(HttpHeaders.LOCATION, StringContains.containsString("code="))
  }

  def 'should be redirected to webPage'() {
    given:
    def setup = given(this.documentationSpec)
        .accept(ContentType.HTML)
        .filter(document('register_redirect_to_web',
        requestParameters(
            parameterWithName('client_id')
                .description('relying party identifier'),
            parameterWithName('scope')
                .description('Scope for authorization that will be provided through JWT to all resource servers in flow'),
            parameterWithName(ProvidersStubConfiguration.REG_CREDENTIAL)
                .description('Some credential fields...')
                .optional()
        ),
        responseHeaders(
            headerWithName(HttpHeaders.LOCATION)
                .description('Header that contains authorization code for the next step of authorization code flow,' +
                '\nits expiration date and optional user identifier')
        )))
        .given()
        .formParam('client_id', PersistenceServiceStubConfiguration.CLIENT_NAME)
        .formParam('scope', 'rs.read')
        .formParam(ProvidersStubConfiguration.REG_CREDENTIAL, 'credential')
    when:
    def result = setup
        .when()
        .get("register")
    then:
    result
        .then()
        .statusCode(HttpStatus.MOVED_PERMANENTLY.value())
        .header(HttpHeaders.LOCATION, StringContains.containsString('scope=rs.read'))
        .header(HttpHeaders.LOCATION, StringContains.containsString('client_id=' + PersistenceServiceStubConfiguration.CLIENT_NAME))
        .header(HttpHeaders.LOCATION, StringContains.containsString('is_webview='))// according to test stub
  }

}

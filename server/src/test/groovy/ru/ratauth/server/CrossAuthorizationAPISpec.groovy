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

import static com.jayway.restassured.RestAssured.given
import static org.hamcrest.Matchers.equalToIgnoringCase
import static org.springframework.restdocs.headers.HeaderDocumentation.*
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters
import static org.springframework.restdocs.restassured.RestAssuredRestDocumentation.document
/**
 * @author djassan
 * @since 11/09/16
 */
class CrossAuthorizationAPISpec extends BaseDocumentationSpec {
  @Value('${server.port}')
  String port
  @Autowired
  ObjectMapper objectMapper

  def 'should successfully return cross-authorization code'() {
    given:
    def setup = given(this.documentationSpec)
      .accept(ContentType.URLENC)
      .filter(document('cross_auth_succeed',
      preprocessResponse(prettyPrint()),
      requestParameters(
        parameterWithName('response_type')
          .description('Response type, this case it must be TOKEN'),
        parameterWithName('grant_type')
          .description('grant type for operation'),
        parameterWithName('scope')
          .description('Scope for authorization that will be provided through JWT to all resource servers in flow'),
        parameterWithName('client_id')
          .description('relying party identifier'),
        parameterWithName('refresh_token')
          .description('refresh token')
      ),
      requestHeaders(
        headerWithName(HttpHeaders.AUTHORIZATION)
          .description('Authorization header for relying party basic authorization')
      ),
      responseHeaders(
        headerWithName(HttpHeaders.LOCATION)
          .description('Header that contains authorization code for the next step of authorization code flow,' +
          '\nits expiration date and optional user identifier')
      )
    ))
      .given()
      .formParam('response_type', AuthzResponseType.CODE.name())
      .formParam('grant_type', GrantType.AUTHENTICATION_TOKEN.name())
      .formParam('client_id', PersistenceServiceStubConfiguration.CLIENT_NAME + '2')
      .formParam('scope', 'rs.read')
      .formParam('refresh_token', PersistenceServiceStubConfiguration.REFRESH_TOKEN)
      .header(IntegrationSpecUtil.createAuthHeaders(PersistenceServiceStubConfiguration.CLIENT_NAME,
      PersistenceServiceStubConfiguration.PASSWORD))
    when:
    def result = setup
      .when()
      .post("authorize")
    then:
    result
      .then()
      .statusCode(HttpStatus.FOUND.value())
      .header(HttpHeaders.LOCATION, StringContains.containsString("code="))
  }

  def 'should successfully get jwt token for external resource server'() {
    given:
    def setup = given(this.documentationSpec)
      .accept(ContentType.URLENC)
      .filter(document('check_token_for_3rd_party_succeed',
      preprocessResponse(prettyPrint()),
      requestParameters(
        parameterWithName('token')
          .description('access token that must be checked'),
        parameterWithName('client_id')
          .description('resource provider identifier')
      ),
      requestHeaders(
        headerWithName(HttpHeaders.AUTHORIZATION)
          .description('Authorization header for relying party basic authorization')
      ),
      responseFields(
          fieldWithPath('jti')
              .description('JWT ID. A unique identifier for the token. The JWT ID MAY be used by implementations requiring message de-duplication for one-time use assertions.')
              .type(JsonFieldType.STRING),
          fieldWithPath('id_token')
              .description('ID Token value associated with the authenticated session.')
              .type(JsonFieldType.STRING),
        fieldWithPath('exp')
          .description('expiration date of checkeed token')
          .type(JsonFieldType.NUMBER),
        fieldWithPath('client_id')
          .description('relying party identifier')
          .type(JsonFieldType.STRING),
        fieldWithPath('scope')
          .description('scopes of access token')
          .type(JsonFieldType.ARRAY)
      )
    ))
      .given()
      .formParam('token', PersistenceServiceStubConfiguration.TOKEN)
      .formParam('client_id', PersistenceServiceStubConfiguration.CLIENT_NAME+'3')
      .header(IntegrationSpecUtil.createAuthHeaders(PersistenceServiceStubConfiguration.CLIENT_NAME,
      PersistenceServiceStubConfiguration.PASSWORD))
    when:
    def result = setup
      .when()
      .post("check_token")
    then:
    result
      .then()
      .statusCode(HttpStatus.OK.value())
      .body("client_id", equalToIgnoringCase(PersistenceServiceStubConfiguration.CLIENT_NAME + '3'))
  }
}

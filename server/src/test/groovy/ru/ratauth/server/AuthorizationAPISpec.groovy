package ru.ratauth.server

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.snakeyaml.external.biz.base64Coder.Base64Coder
import com.jayway.restassured.http.ContentType
import com.jayway.restassured.response.Header
import org.hamcrest.core.StringContains
import org.hamcrest.text.IsEqualIgnoringCase
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.IntegrationTest
import org.springframework.boot.test.SpringApplicationConfiguration
import org.springframework.http.*
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.test.context.TestPropertySource
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import ratpack.test.ApplicationUnderTest
import ratpack.test.http.TestHttpClient
import ru.ratauth.exception.BaseAuthServerException
import ru.ratauth.exception.ExpiredException
import ru.ratauth.interaction.AuthzResponseType
import ru.ratauth.interaction.GrantType
import ru.ratauth.server.configuration.PersistenceServiceStubConfiguration
import ru.ratauth.server.handlers.dto.CheckTokenDTO
import ru.ratauth.server.handlers.dto.TokenDTO
import spock.lang.Shared
import spock.lang.Specification

import java.nio.charset.Charset

import static org.hamcrest.Matchers.containsString
import static org.hamcrest.Matchers.equalToIgnoringCase
import static org.hamcrest.Matchers.notNullValue
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders
import static org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters
import static org.springframework.restdocs.restassured.RestAssuredRestDocumentation.document
import static org.springframework.restdocs.restassured.RestAssuredRestDocumentation.documentationConfiguration
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import static com.jayway.restassured.RestAssured.given

/**
 * @author mgorelikov
 * @since 03/11/15
 */

class AuthorizationAPISpec extends BaseDocumentationSpec {
  @Value('${server.port}')
  String port
  @Autowired
  ObjectMapper objectMapper


  def 'should get authorization code'() {
    given:
    def setup = given(this.documentationSpec)
        .accept(ContentType.URLENC)
        .filter(document('auth_code_succeed',
        requestParameters(
            parameterWithName('response_type')
                .description('Response type that must be provided CODE or TOKEN'),
            parameterWithName('client_id')
                .description('relying party identifier'),
            parameterWithName('scope')
                .description('Scope for authorization that will be provided through JWT to all resource servers in flow'),
            parameterWithName('username')
                .description('part of user\'s credentials'),
            parameterWithName('password')
                .description('part of user\'s credentials')
                .optional()
        ),
        responseHeaders(
            headerWithName(HttpHeaders.LOCATION)
                .description('Header that contains authorization code for the next step of authorization code flow,' +
                '\nits expiration date and optional user identifier')
        )))
        .given()
        .formParam('response_type', AuthzResponseType.CODE.name())
        .formParam('client_id', PersistenceServiceStubConfiguration.CLIENT_NAME)
        .formParam('scope', 'rs.read')
        .formParam('username', 'login')
        .formParam('password', 'password')
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

  def 'should return bad request status for authorization code request'() {
    given:
    def setup = given(this.documentationSpec)
        .accept(ContentType.URLENC)
        .filter(document('auth_code_bad_request',
        preprocessResponse(prettyPrint()),
        responseFields(
            fieldWithPath('id')
                .description('Error identifier')
                .type(JsonFieldType.STRING),
            fieldWithPath('message')
                .description('Different localizations of message')
                .type(JsonFieldType.OBJECT),
            fieldWithPath('base_id')
                .description('Base identifier for auth module exceptions')
                .type(JsonFieldType.STRING),
            fieldWithPath('type_id')
                .description('String identifier of exception type')
                .type(JsonFieldType.STRING),
            fieldWithPath('class')
                .description('Exception class')
                .type(JsonFieldType.STRING),
        )))
        .given()
        .formParam('client_id', PersistenceServiceStubConfiguration.CLIENT_NAME)
        .formParam('scope', 'rs.read')
        .formParam('username', 'login')
        .formParam('password', 'password')
    when:
    def result = setup
        .when()
        .post("authorize")
    then:
    result
        .then()
        .statusCode(HttpStatus.BAD_REQUEST.value())
        .body("message.en", containsString("response_type"))
  }

  def 'should return bad requisites status for authorization code request'() {
    given:
    def setup = given(this.documentationSpec)
        .accept(ContentType.URLENC)
        .filter(document('auth_code_forbidden',
        preprocessResponse(prettyPrint()),
        responseFields(
            fieldWithPath('id')
                .description('Error identifier')
                .type(JsonFieldType.STRING),
            fieldWithPath('message')
                .description('Different localizations of message')
                .type(JsonFieldType.OBJECT),
            fieldWithPath('base_id')
                .description('Base identifier for auth module exceptions')
                .type(JsonFieldType.STRING),
            fieldWithPath('type_id')
                .description('String identifier of exception type')
                .type(JsonFieldType.STRING),
            fieldWithPath('class')
                .description('Exception class')
                .type(JsonFieldType.STRING),
        )))
        .given()
        .formParam('response_type', AuthzResponseType.CODE.name())
        .formParam('client_id', PersistenceServiceStubConfiguration.CLIENT_NAME)
        .formParam('scope', 'rs.read')
        .formParam('username', 'login')
        .formParam('password', 'bad')
    when:
    def result = setup
        .when()
        .post("authorize")
    then:
    result
        .then()
        .statusCode(HttpStatus.FORBIDDEN.value())
        .body("type_id", equalToIgnoringCase(BaseAuthServerException.Type.AUTHORIZATION.name()))
  }

  def 'should successfully return request token'() {
    given:
    def setup = given(this.documentationSpec)
        .accept(ContentType.URLENC)
        .filter(document('token_succeed',
        preprocessResponse(prettyPrint()),
        requestParameters(
            parameterWithName('response_type')
                .description('Response type that must be provided'),
            parameterWithName('grant_type')
                .description('grant type for token request'),
            parameterWithName('code')
                .description('authorization code'),
        ),
        requestHeaders(
            headerWithName(HttpHeaders.AUTHORIZATION)
                .description('Authorization header for relying party basic authorization')
        ),
        responseFields(
            fieldWithPath('access_token')
                .description('Access token')
                .type(JsonFieldType.STRING),
            fieldWithPath('refresh_token')
                .description('token that can be used to refresh expired access token')
                .type(JsonFieldType.STRING),
            fieldWithPath('token_type')
                .description('token type according to OpenID specification')
                .type(JsonFieldType.STRING),
            fieldWithPath('id_token')
                .description('JWT token')
                .type(JsonFieldType.STRING),
            fieldWithPath('expires_in')
                .description('expiration date of access token')
                .type(JsonFieldType.NUMBER),
            fieldWithPath('client_id')
                .description('identifier of relying party')
                .type(JsonFieldType.STRING),
        )))
        .given()
        .formParam('grant_type', GrantType.AUTHORIZATION_CODE.name())
        .formParam('response_type', AuthzResponseType.TOKEN.name())
        .formParam('code', PersistenceServiceStubConfiguration.CODE)
        .header(createAuthHeaders(PersistenceServiceStubConfiguration.CLIENT_NAME, PersistenceServiceStubConfiguration.PASSWORD))
    when:
    def result = setup
        .when()
        .post("token")
    then:
    result
        .then()
        .statusCode(HttpStatus.OK.value())
        .body("access_token", notNullValue())
  }

  def 'request token by expired code'() {
    given:
    def setup = given(this.documentationSpec)
        .accept(ContentType.URLENC)
        .filter(document('token_code_expired',
        preprocessResponse(prettyPrint()),
        requestHeaders(
            headerWithName(HttpHeaders.AUTHORIZATION)
                .description('Authorization header for relying party basic authorization')
        ),
        responseFields(
            fieldWithPath('id')
                .description('Error identifier')
                .type(JsonFieldType.STRING),
            fieldWithPath('message')
                .description('Different localizations of message')
                .type(JsonFieldType.OBJECT),
            fieldWithPath('base_id')
                .description('Base identifier for auth module exceptions')
                .type(JsonFieldType.STRING),
            fieldWithPath('type_id')
                .description('String identifier of exception type')
                .type(JsonFieldType.STRING),
            fieldWithPath('class')
                .description('Exception class')
                .type(JsonFieldType.STRING),
        )))
        .given()
        .formParam('grant_type', GrantType.AUTHORIZATION_CODE.name())
        .formParam('response_type', AuthzResponseType.TOKEN.name())
        .formParam('code', PersistenceServiceStubConfiguration.CODE_EXPIRED)
        .header(createAuthHeaders(PersistenceServiceStubConfiguration.CLIENT_NAME, PersistenceServiceStubConfiguration.PASSWORD))
    when:
    def result = setup
        .when()
        .post("token")
    then:
    result
        .then()
        .statusCode(HttpStatus.INSUFFICIENT_SPACE_ON_RESOURCE.value())//resource expired
        .body("id", equalToIgnoringCase(ExpiredException.ID.AUTH_CODE_EXPIRED.name()))
  }

  def 'should successfully return token by implicit flow'() {
    given:
    def setup = given(this.documentationSpec)
        .accept(ContentType.URLENC)
        .filter(document('token_implicit_succeed',
        requestParameters(
            parameterWithName('response_type')
                .description('Response type that must be provided CODE or TOKEN'),
            parameterWithName('scope')
                .description('Scope for authorization that will be provided through JWT to all resource servers in flow'),
            parameterWithName('username')
                .description('part of user\'s credentials'),
            parameterWithName('password')
                .description('part of user\'s credentials')
                .optional()
        ),
        requestHeaders(
            headerWithName(HttpHeaders.AUTHORIZATION)
                .description('Authorization header for relying party basic authorization')
        ),
        responseHeaders(
            headerWithName(HttpHeaders.LOCATION)
                .description('Header that contains token, refresh token and expiration date of access token')
        )))
        .given()
        .formParam('response_type', AuthzResponseType.TOKEN.name())
        .formParam('scope', 'read')
        .formParam('username', 'login')
        .formParam('password', 'password')
        .header(createAuthHeaders(PersistenceServiceStubConfiguration.CLIENT_NAME, PersistenceServiceStubConfiguration.PASSWORD))
    when:
    def result = setup
        .when()
        .post("authorize")
    then:
    result
        .then()
        .statusCode(HttpStatus.FOUND.value())
        .header(HttpHeaders.LOCATION, StringContains.containsString("token="))
  }

  def 'refresh token'() {
    given:
    def setup = given(this.documentationSpec)
        .accept(ContentType.URLENC)
        .filter(document('token_refresh_succeed',
        preprocessResponse(prettyPrint()),
        requestParameters(
            parameterWithName('response_type')
                .description('Response type, this case it must be TOKEN'),
            parameterWithName('grant_type')
                .description('grant type for operation'),
            parameterWithName('refresh_token')
                .description('refresh token')
        ),
        requestHeaders(
            headerWithName(HttpHeaders.AUTHORIZATION)
                .description('Authorization header for relying party basic authorization')
        ),
        responseFields(
            fieldWithPath('access_token')
                .description('Access token')
                .type(JsonFieldType.STRING),
            fieldWithPath('refresh_token')
                .description('token that can be used to refresh expired access token')
                .type(JsonFieldType.STRING),
            fieldWithPath('token_type')
                .description('token type according to OpenID specification')
                .type(JsonFieldType.STRING),
            fieldWithPath('id_token')
                .description('JWT token')
                .type(JsonFieldType.STRING),
            fieldWithPath('expires_in')
                .description('expiration date of access token')
                .type(JsonFieldType.NUMBER),
            fieldWithPath('client_id')
                .description('identifier of relying party')
                .type(JsonFieldType.STRING),
        )
    ))
        .given()
        .formParam('response_type', AuthzResponseType.TOKEN.name())
        .formParam('grant_type', GrantType.REFRESH_TOKEN.name())
        .formParam('refresh_token', PersistenceServiceStubConfiguration.REFRESH_TOKEN)
        .header(createAuthHeaders(PersistenceServiceStubConfiguration.CLIENT_NAME, PersistenceServiceStubConfiguration.PASSWORD))
    when:
    def result = setup
        .when()
        .post("token")
    then:
    result
        .then()
        .statusCode(HttpStatus.OK.value())
        .body("access_token", notNullValue())
  }

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
        .header(createAuthHeaders(PersistenceServiceStubConfiguration.CLIENT_NAME, PersistenceServiceStubConfiguration.PASSWORD))
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

  def 'should successfully check token'() {
    given:
    def setup = given(this.documentationSpec)
        .accept(ContentType.URLENC)
        .filter(document('check_token_succeed',
        preprocessResponse(prettyPrint()),
        requestParameters(
            parameterWithName('token')
                .description('access token that must be checked'),
        ),
        requestHeaders(
            headerWithName(HttpHeaders.AUTHORIZATION)
                .description('Authorization header for relying party basic authorization')
        ),
        responseFields(
            fieldWithPath('jti')
                .description('JWT token')
                .type(JsonFieldType.STRING),
            fieldWithPath('exp')
                .description('expiration date of checkeed token')
                .type(JsonFieldType.STRING),
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
        .header(createAuthHeaders(PersistenceServiceStubConfiguration.CLIENT_NAME, PersistenceServiceStubConfiguration.PASSWORD))
    when:
    def result = setup
        .when()
        .post("check_token")
    then:
    result
        .then()
        .statusCode(HttpStatus.OK.value())
        .body("client_id", equalToIgnoringCase(PersistenceServiceStubConfiguration.CLIENT_NAME))
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
                .description('JWT token')
                .type(JsonFieldType.STRING),
            fieldWithPath('exp')
                .description('expiration date of checkeed token')
                .type(JsonFieldType.STRING),
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
        .header(createAuthHeaders(PersistenceServiceStubConfiguration.CLIENT_NAME, PersistenceServiceStubConfiguration.PASSWORD))
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

  private Header createAuthHeaders(String username, String password) {
    def auth = username + ":" + password;
    def encodedAuth =
        Base64Coder.encode(auth.getBytes(Charset.forName("UTF-8")))
    def authHeader = "Basic " + new String(encodedAuth)
    return new Header(HttpHeaders.AUTHORIZATION, authHeader)
  }
}

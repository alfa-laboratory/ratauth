package ru.ratauth.server

import com.fasterxml.jackson.databind.ObjectMapper
import com.jayway.restassured.http.ContentType
import org.hamcrest.core.StringContains
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.restdocs.payload.JsonFieldType
import ru.ratauth.exception.BaseAuthServerException
import ru.ratauth.exception.ExpiredException
import ru.ratauth.interaction.AuthzResponseType
import ru.ratauth.interaction.GrantType
import ru.ratauth.server.local.PersistenceServiceStubConfiguration

import static com.jayway.restassured.RestAssured.given
import static org.hamcrest.Matchers.*
import static org.springframework.restdocs.headers.HeaderDocumentation.*
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import static org.springframework.restdocs.request.RequestDocumentation.partWithName
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters
import static org.springframework.restdocs.restassured.RestAssuredRestDocumentation.document

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
                .description('part of user\'s credentials'),
                parameterWithName('acr_values')
                        .description('Authentication Context Class Reference'),
                parameterWithName('enroll')
                        .description('Required Authentication Context Class Reference')
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
            .formParam('acr_values', 'username')
            .formParam('enroll', 'username')
    when:
    def result = setup
        .when()
        .post("authorize")
    then:
    result
        .then()
        .statusCode(HttpStatus.FOUND.value())
        .header(HttpHeaders.LOCATION, StringContains.containsString("code="))
            .header(HttpHeaders.LOCATION, StringContains.containsString("session_token="))
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
            .formParam('acr_values', 'username')
            .formParam('enroll', 'username')
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
      .header(IntegrationSpecUtil.createAuthHeaders(PersistenceServiceStubConfiguration.CLIENT_NAME,
                                                    PersistenceServiceStubConfiguration.PASSWORD))
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
        .header(IntegrationSpecUtil.createAuthHeaders(PersistenceServiceStubConfiguration.CLIENT_NAME,
                                                      PersistenceServiceStubConfiguration.PASSWORD))
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
                .description('part of user\'s credentials'),
                parameterWithName('acr_values')
                        .description('Authentication Context Class Reference'),
                parameterWithName('enroll')
                        .description('Required Authentication Context Class Reference')
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
            .formParam('acr_values', 'username')
            .formParam('enroll', 'username')
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
        .header(HttpHeaders.LOCATION, StringContains.containsString("token="))
  }

  def 'should not allow non-correct redirect url' () {
    given:
    def setup = given(this.documentationSpec)
        .accept(ContentType.URLENC)
        .filter(document('authorize_not_allowed_redirect',
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
                .description('part of user\'s credentials'),
                parameterWithName('acr_values')
                        .description('Authentication Context Class Reference'),
                parameterWithName('enroll')
                        .description('Required Authentication Context Class Reference')
                .optional(),
            parameterWithName('redirect_uri')
                .description('end-user will be redirected on that URI with authorization code')
                .optional()
        )))
        .given()
        .formParam('response_type', AuthzResponseType.CODE.name())
        .formParam('client_id', PersistenceServiceStubConfiguration.CLIENT_NAME)
        .formParam('scope', 'rs.read')
        .formParam('username', 'login')
        .formParam('password', 'password')
        .formParam('redirect_uri', 'http://blabla.com')
            .formParam('acr_values', 'username')
            .formParam('enroll', 'username')
    when:
    def result = setup
        .when()
        .post("authorize")
    then:
    result
        .then()
        .statusCode(HttpStatus.FORBIDDEN.value())
  }

  def 'should be redirected ToWeb'() {
    given:
    def setup = given(this.documentationSpec)
        .accept(ContentType.HTML)
        .filter(document('authorize_redirect_to_web',
        requestParameters(
            parameterWithName('response_type')
                .description('Response type that must be provided CODE or TOKEN'),
            parameterWithName('client_id')
                .description('relying party identifier'),
            parameterWithName('scope')
                .description('Scope for authorization that will be provided through JWT to all resource servers in flow'),
                parameterWithName('acr_values')
                .description('Authentication context class reference'),
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
        .formParam('acr_values', 'card')
        .formParam('username', 'login')
        .formParam('password', 'password')
    when:
    def result = setup
        .when()
        .get("authorize")
    then:
    result
        .then()
        .statusCode(HttpStatus.FOUND.value())
        .header(HttpHeaders.LOCATION, StringContains.containsString('scope=rs.read'))
        .header(HttpHeaders.LOCATION, StringContains.containsString('username=login'))
        .header(HttpHeaders.LOCATION, StringContains.containsString('client_id=' + PersistenceServiceStubConfiguration.CLIENT_NAME))
        .header(HttpHeaders.LOCATION, StringContains.containsString('is_webview='))// according to test stub
        .header(HttpHeaders.LOCATION, StringContains.containsString('http://domain.mine/oidc/web/authorize/card?is_webview=true&response_type=CODE&client_id=mine&scope=rs.read&acr_values=card&username=login&password=password'))

  }
}

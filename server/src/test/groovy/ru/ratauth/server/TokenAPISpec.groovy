package ru.ratauth.server

import com.fasterxml.jackson.databind.ObjectMapper
import com.jayway.restassured.http.ContentType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.restdocs.payload.JsonFieldType
import ru.ratauth.exception.AuthorizationException
import ru.ratauth.interaction.AuthzResponseType
import ru.ratauth.interaction.GrantType
import ru.ratauth.server.local.PersistenceServiceStubConfiguration
import ru.ratauth.server.services.log.AuthAction

import static com.jayway.restassured.RestAssured.given
import static org.hamcrest.Matchers.*
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders
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
class TokenAPISpec extends BaseDocumentationSpec {
    @Value('${server.port}')
    String port
    @Autowired
    ObjectMapper objectMapper

    def 'refresh token (old scheme)'() {
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
                .formParam('refresh_token', PersistenceServiceStubConfiguration.REFRESH_TOKEN_OLD_SCHEME)
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

    def 'refresh token using default response type'() {
        given:
        def setup = given(this.documentationSpec)
                .accept(ContentType.URLENC)
                .filter(document('token_refresh_succeed',
                preprocessResponse(prettyPrint()),
                requestParameters(
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
                .formParam('grant_type', GrantType.REFRESH_TOKEN.name())
                .formParam('refresh_token', PersistenceServiceStubConfiguration.REFRESH_TOKEN)
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
                                .description('scope of access token')
                                .type(JsonFieldType.ARRAY)
                )
        ))
                .given()
                .formParam('token', PersistenceServiceStubConfiguration.TOKEN)
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
                .body("client_id", equalToIgnoringCase(PersistenceServiceStubConfiguration.CLIENT_NAME))
    }

    def 'should basic auth failed with good token'() {
        given:
        def setup = given(this.documentationSpec)
                .accept(ContentType.URLENC)
                .given()
                .formParam('token', PersistenceServiceStubConfiguration.TOKEN)
                .header(IntegrationSpecUtil.createAuthHeaders(PersistenceServiceStubConfiguration.NONEXISTENT_CLIENT_NAME, 'bad_password'))

        when:
        def result = setup
                .when()
                .post("check_token")

        then:
        result
                .then()
                .statusCode(HttpStatus.FORBIDDEN.value())
                .body('id', equalTo(AuthorizationException.ID.CLIENT_NOT_FOUND.name()))
                .body('message.en', equalTo(AuthorizationException.ID.CLIENT_NOT_FOUND.baseText))
                .body('type_id', equalTo(AuthAction.AUTHORIZATION.name()))
                .body('class', equalTo('class ' + AuthorizationException.class.name))
    }

    def 'should basic auth failed with bad token'() {
        given:
        def setup = given(this.documentationSpec)
                .accept(ContentType.URLENC)
                .given()
                .formParam('token', "bad_token")
                .header(IntegrationSpecUtil.createAuthHeaders(PersistenceServiceStubConfiguration.NONEXISTENT_CLIENT_NAME, 'bad_password_1'))

        when:
        def result = setup
                .when()
                .post("check_token")

        then:
        result
                .then()
                .statusCode(HttpStatus.FORBIDDEN.value())
                .body('id', equalTo(AuthorizationException.ID.CLIENT_NOT_FOUND.name()))
                .body('message.en', equalTo(AuthorizationException.ID.CLIENT_NOT_FOUND.baseText))
                .body('type_id', equalTo(AuthAction.AUTHORIZATION.name()))
                .body('class', equalTo('class ' + AuthorizationException.class.name))
    }
}

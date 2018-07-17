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
import static java.net.URLEncoder.*
import static org.hamcrest.Matchers.contains
import static org.hamcrest.Matchers.containsInAnyOrder
import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.equalToIgnoringCase
import static org.hamcrest.Matchers.notNullValue
import static org.hamcrest.Matchers.startsWith
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
class EnrollAPISpec extends BaseDocumentationSpec {

    @Value('${server.port}')
    String port
    @Autowired
    ObjectMapper objectMapper

    def 'activate enroll'() {
        given:
        def setup = given(this.documentationSpec)
                .accept(ContentType.URLENC)
                .filter(document('mfa_activate_succeed',
                preprocessResponse(prettyPrint()),
                requestParameters(
                        parameterWithName('mfa_token')
                                .description('MFA token issued for permission update'),
                        parameterWithName('client_id')
                                .description('Relying party identifier'),
                        parameterWithName('scope')
                                .description('Scope for authorization that will be provided through JWT to all resource servers in flow'),
                        parameterWithName('acr_values')
                                .description('Authentication Context Class Reference'),
                        parameterWithName('enroll')
                                .description('Required Authentication Context Class Reference'),
                ),
                responseFields(
                        fieldWithPath('mfa_token')
                                .description('MFA token')
                                .type(JsonFieldType.STRING),
                        fieldWithPath('data')
                                .description('Activation result from provider')
                                .type(JsonFieldType.OBJECT)
                )
        ))
                .given()
                .formParam('mfa_token', PersistenceServiceStubConfiguration.MFA_TOKEN)
                .formParam('client_id', PersistenceServiceStubConfiguration.CLIENT_NAME)
                .formParam('scope', 'rs.read')
                .formParam('acr_values', 'username:sms')
                .formParam('enroll', 'username')
        when:
        def result = setup
                .when()
                .post("activate")
        then:
        result
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("mfa_token", equalTo("mfa-token-test"))
    }

    def 'activate enroll no session'() {
        given:
        def setup = given(this.documentationSpec)
                .accept(ContentType.URLENC)
                .filter(document('mfa_activate_succeed',
                preprocessResponse(prettyPrint()),
                requestParameters(
                        parameterWithName('client_id')
                                .description('Relying party identifier'),
                        parameterWithName('scope')
                                .description('Scope for authorization that will be provided through JWT to all resource servers in flow'),
                        parameterWithName('acr_values')
                                .description('Authentication Context Class Reference'),
                        parameterWithName('enroll')
                                .description('Required Authentication Context Class Reference'),
                        parameterWithName('username')
                                .description("User data"),
                ),
                responseFields(
                        fieldWithPath('data')
                                .description('Activation result from provider')
                                .type(JsonFieldType.OBJECT)
                )
        ))
                .given()
                .formParam('client_id', PersistenceServiceStubConfiguration.CLIENT_NAME)
                .formParam('username', "user")
                .formParam('scope', 'rs.read')
                .formParam('acr_values', 'username:sms')
                .formParam('enroll', 'username')
        when:
        def result = setup
                .when()
                .post("activate")
        then:
        result
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.user_id", equalTo("resu"))
    }



    def 'activate enroll with data'() {
        given:
        def setup = given(this.documentationSpec)
                .accept(ContentType.URLENC)
                .filter(document('mfa_activate_succeed',
                preprocessResponse(prettyPrint()),
                requestParameters(
                        parameterWithName('mfa_token')
                                .description('MFA token issued for permission update'),
                        parameterWithName('client_id')
                                .description('Relying party identifier'),
                        parameterWithName('username')
                                .description('part of user\'s credentials'),
                        parameterWithName('scope')
                                .description('Scope for authorization that will be provided through JWT to all resource servers in flow'),
                        parameterWithName('acr_values')
                                .description('Authentication Context Class Reference'),
                        parameterWithName('enroll')
                                .description('Required Authentication Context Class Reference'),

                ),
                responseFields(
                        fieldWithPath('mfa_token')
                                .description('MFA token')
                                .type(JsonFieldType.STRING),
                        fieldWithPath('data')
                                .description('Activation result from provider')
                                .type(JsonFieldType.OBJECT)
                )
        ))
                .given()
                .formParam("username", "username")
                .formParam('mfa_token', PersistenceServiceStubConfiguration.MFA_TOKEN)
                .formParam('client_id', PersistenceServiceStubConfiguration.CLIENT_NAME)
                .formParam('scope', 'rs.read')
                .formParam('acr_values', 'username:sms')
                .formParam('enroll', 'username')
        when:
        def result = setup
                .when()
                .post("activate")
        then:
        result
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("mfa_token", equalTo("mfa-token-test"))
                .body("data", notNullValue())
    }

    def 'verify enroll with correct data'() {
        given:
        def setup = given(this.documentationSpec)
                .accept(ContentType.URLENC)
                .filter(document('mfa_activate_succeed',
                preprocessResponse(prettyPrint()),
                requestParameters(
                        parameterWithName('mfa_token')
                                .description('MFA token issued for permission update'),
                        parameterWithName('client_id')
                                .description('Relying party identifier'),
                        parameterWithName('username')
                                .description('part of user\'s credentials'),
                        parameterWithName('password')
                                .description('part of user\'s credentials'),
                        parameterWithName('scope')
                                .description('Scope for authorization that will be provided through JWT to all resource servers in flow'),
                        parameterWithName('state')
                                .description('State from RP'),
                        parameterWithName('acr_values')
                                .description('Authentication Context Class Reference'),
                        parameterWithName('enroll')
                                .description('Required Authentication Context Class Reference'),

                )
        ))
                .given()
                .formParam("username", "username")
                .formParam("password", "password")
                .formParam('mfa_token', PersistenceServiceStubConfiguration.MFA_TOKEN)
                .formParam('client_id', PersistenceServiceStubConfiguration.CLIENT_NAME)
                .formParam('scope', 'rs.read')
                .formParam('state', 'relyingpartystate')
                .formParam('acr_values', 'username:sms')
                .formParam('enroll', 'username')
        when:
        def result = setup
                .when()
                .post("verify")
        then:
        result
                .then()
                .statusCode(HttpStatus.FOUND.value())
                .header(HttpHeaders.LOCATION, StringContains.containsString('mfa_token='))
                .header(HttpHeaders.LOCATION, StringContains.containsString('state=relyingpartystate'))
                .header(HttpHeaders.LOCATION, StringContains.containsString("acr_values=${encode('username:sms', 'UTF-8')}"))
                .header(HttpHeaders.LOCATION, StringContains.containsString('http://localhost:8080/domain.mine/oidc/web/authorize/username?is_webview=true&session_token=session_token&scope=rs.read&acr_values=username%3Asms&mfa_token=mfa-token-test&state=relyingpartystate&client_id=mine'))
    }


    def 'verify enroll final step'() {
        given:
        def setup = given(this.documentationSpec)
                .accept(ContentType.URLENC)
                .filter(document('mfa_activate_succeed',
                preprocessResponse(prettyPrint()),
                requestParameters(
                        parameterWithName('mfa_token')
                                .description('MFA token issued for permission update'),
                        parameterWithName('redirect_uri')
                                .description('Redirect URL'),
                        parameterWithName('client_id')
                                .description('Relying party identifier'),
                        parameterWithName('username')
                                .description('part of user\'s credentials'),
                        parameterWithName('password')
                                .description('part of user\'s credentials'),
                        parameterWithName('scope')
                                .description('Scope for authorization that will be provided through JWT to all resource servers in flow'),
                        parameterWithName('acr_values')
                                .description('Authentication Context Class Reference'),
                        parameterWithName('enroll')
                                .description('Required Authentication Context Class Reference'),

                )
        ))
                .given()
                .formParam("username", "username")
                .formParam("password", "password")
                .formParam("redirect_uri", "https://domain.mine/login")
                .formParam('mfa_token', PersistenceServiceStubConfiguration.MFA_TOKEN)
                .formParam('client_id', PersistenceServiceStubConfiguration.CLIENT_NAME)
                .formParam('scope', 'rs.read')
                .formParam('acr_values', 'username')
                .formParam('enroll', 'username')
        when:
        def result = setup
                .when()
                .post("verify")
        then:
        result
                .then()
                .statusCode(HttpStatus.FOUND.value())
                .header(HttpHeaders.LOCATION, startsWith("https://domain.mine/login?"))
                .header(HttpHeaders.LOCATION, StringContains.containsString("code=code"))
                .header(HttpHeaders.LOCATION, StringContains.containsString("session_token=session_token"))
                .header(HttpHeaders.LOCATION, StringContains.containsString('https://domain.mine/login?session_token=session_token&code=code&expires_in=36000'))
    }
}

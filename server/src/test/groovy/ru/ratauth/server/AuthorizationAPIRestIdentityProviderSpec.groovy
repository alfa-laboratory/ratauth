package ru.ratauth.server

import com.github.tomakehurst.wiremock.junit.WireMockRule
import com.jayway.restassured.http.ContentType
import groovy.json.JsonOutput
import io.netty.handler.codec.http.HttpResponseStatus
import org.eclipse.jetty.http.HttpHeader
import org.hamcrest.core.StringContains
import org.junit.Rule
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import ru.ratauth.interaction.AuthzResponseType
import ru.ratauth.server.local.PersistenceServiceStubConfiguration

import static com.github.tomakehurst.wiremock.client.WireMock.*
import static com.jayway.restassured.RestAssured.given
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName
import static org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters
import static org.springframework.restdocs.restassured.RestAssuredRestDocumentation.document

class AuthorizationAPIRestIdentityProviderSpec extends BaseDocumentationSpec {

    @Value('${server.port}')
    String port

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8089);

    def 'should get authorization code by rest provider'() {
        given:
        mockRestProviderWithResponse([status: 'SUCCESS', data: [user_id: "USER"]])
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
                                .description('''\
Header that contains authorization code for the next step of 
authorization code flow, its expiration date and optional user 
identifier received by REST-based identity provider''')
                )))
                .given()
                .formParam('response_type', AuthzResponseType.CODE.name())
                .formParam('client_id', PersistenceServiceStubConfiguration.CLIENT_NAME_REST)
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
    }

    def 'should get error by rest provider without body'() {
        given:
        mockRestProviderWithErrorResponse()
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
                )))
                .given()
                .formParam('response_type', AuthzResponseType.CODE.name())
                .formParam('client_id', PersistenceServiceStubConfiguration.CLIENT_NAME_REST)
                .formParam('scope', 'rs.read')
                .formParam('username', 'unprocessable_login')
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
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .body(StringContains.containsString("ProviderException"))
    }


    def 'should get authorization error by rest provider when invalid login'() {
        given:
        mockRestProviderWithInvalidLoginResponse()
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
                )))
                .given()
                .formParam('response_type', AuthzResponseType.CODE.name())
                .formParam('client_id', PersistenceServiceStubConfiguration.CLIENT_NAME_REST)
                .formParam('scope', 'rs.read')
                .formParam('username', 'invalid_login')
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
                .statusCode(HttpStatus.FORBIDDEN.value())
                .body(StringContains.containsString("AuthorizationException"))
    }

    private static void mockRestProviderWithResponse(mockResponse) {
        assert mockResponse.data["user_id"]
        stubFor(post(
                urlEqualTo("/verify"))
                .withHeader(HttpHeader.CONTENT_TYPE.asString(), equalTo(MediaType.APPLICATION_JSON_VALUE))
                .withBasicAuth("llogin", "ppassword")
                .withRequestBody(equalTo("data.username=login&data.enroll=username&relying_party=DummyIdentityProvider&data.password=password&data.acr_values=username&enroll=username"))
                .willReturn(
                aResponse()
                        .withStatus(HttpResponseStatus.OK.code())
                        .withHeader(HttpHeader.CONTENT_TYPE.asString(), MediaType.APPLICATION_JSON_VALUE)
                        .withBody(JsonOutput.toJson([mockResponse]))
        ))
    }


    private static void mockRestProviderWithErrorResponse() {
        stubFor(post(
                urlEqualTo("/verify"))
                .withHeader(HttpHeader.CONTENT_TYPE.asString(), equalTo(MediaType.APPLICATION_JSON_VALUE))
                .withBasicAuth("llogin", "ppassword")
                .withRequestBody(equalTo("data.username=unprocessable_login&data.enroll=username&relying_party=DummyIdentityProvider&data.password=password&data.acr_values=username&enroll=username"))
                .willReturn(
                aResponse()
                        .withStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR.code())
        ))
    }

    private static void mockRestProviderWithInvalidLoginResponse() {
        stubFor(post(
                urlEqualTo("/verify"))
                .withHeader(HttpHeader.CONTENT_TYPE.asString(), equalTo(MediaType.APPLICATION_JSON_VALUE))
                .withBasicAuth("llogin", "ppassword")
                .withRequestBody(equalTo("data.username=invalid_login&data.enroll=username&relying_party=DummyIdentityProvider&data.password=password&data.acr_values=username&enroll=username"))
                .willReturn(
                aResponse()
                        .withStatus(HttpResponseStatus.FORBIDDEN.code())
        ))
    }
}
package ru.ratauth.server.handlers

import io.restassured.http.ContentType
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import ru.ratauth.server.BaseDocumentationSpec
import ru.ratauth.server.IntegrationSpecUtil
import ru.ratauth.server.local.PersistenceServiceStubConfiguration

import static io.restassured.RestAssured.given
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters
import static org.springframework.restdocs.restassured3.RestAssuredRestDocumentation.document
import static ru.ratauth.server.handlers.SessionHandler.INVALIDATE_SESSION_BY_REFRESH_TOKEN

class SessionHandlerSpec extends BaseDocumentationSpec {

    def 'should successfully invalidate session by refresh token'() {
        given:
        def setup = given(this.documentationSpec)
                .accept(ContentType.URLENC)
                .filter(document('session_invalidation_succeed',
                preprocessResponse(prettyPrint()),
                requestParameters(
                        parameterWithName('refresh_token')
                                .description('Invalidated session search refresh token'),
                ),
                requestHeaders(
                        headerWithName(HttpHeaders.AUTHORIZATION)
                                .description('Authorization header for relying party basic authorization')
                )
        ))
                .given()
                .formParam('refresh_token', PersistenceServiceStubConfiguration.REFRESH_TOKEN)
                .header(IntegrationSpecUtil.createAuthHeaders(PersistenceServiceStubConfiguration.CLIENT_NAME,
                PersistenceServiceStubConfiguration.PASSWORD))
        when:
        def result = setup
                .when()
                .post(INVALIDATE_SESSION_BY_REFRESH_TOKEN)
        then:
        result
                .then()
                .statusCode(HttpStatus.OK.value())
    }
}

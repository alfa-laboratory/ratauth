package ru.ratauth.server.handlers

import com.jayway.restassured.http.ContentType
import groovy.json.JsonSlurper
import org.hamcrest.core.StringContains
import org.springframework.http.HttpStatus
import ru.ratauth.server.BaseDocumentationSpec

import static com.jayway.restassured.RestAssured.given
import static org.springframework.http.HttpHeaders.LOCATION
import static org.springframework.http.HttpStatus.FOUND
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName
import static org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters
import static org.springframework.restdocs.restassured.RestAssuredRestDocumentation.document
import static ru.ratauth.server.local.PersistenceServiceStubConfiguration.CLIENT_NAME
import static ru.ratauth.server.local.PersistenceServiceStubConfiguration.UPDATE_CODE_SUCCESS_CASE
import static ru.ratauth.server.local.PersistenceServiceStubConfiguration.UPDATE_CODE_UNPROСESSABLE_EXCEPTION_CASE
import static ru.ratauth.server.local.PersistenceServiceStubConfiguration.UPDATE_CODE_VALIDATION_EXCEPTION_CASE

class UpdateHandlerSpec extends BaseDocumentationSpec {

    def 'success update data by code and get redirect uri with authorization code'() {
        given:
        def setup = given(this.documentationSpec)
                .accept(ContentType.URLENC)
                .filter(document('update_data_by_code_succeed',
                requestParameters(
                        parameterWithName('client_id')
                                .description('relying party identifier'),
                        parameterWithName('login')
                                .description('user login'),
                        parameterWithName('old_password')
                                .description('current user password'),
                        parameterWithName('new_password')
                                .description(''),
                        parameterWithName('reason')
                                .description('what happens'),
                        parameterWithName('update_code')
                                .description('code for update data'),
                        parameterWithName('update_service')
                                .description('name of update service'),
                        parameterWithName('skip')
                                .description('skip update if true')
                ), responseHeaders(
                headerWithName(LOCATION)
                        .description('Header that contains authorization code for the next step of authorization code flow,' +
                        '\nits expiration date and optional user identifier'))))
                .given()
                .formParam('client_id', CLIENT_NAME)
                .formParam('login', 'XAPA6O')
                .formParam('old_password', 'test1')
                .formParam('new_password', 'test2')
                .formParam('reason', 'password_is_temporary')
                .formParam('update_code', UPDATE_CODE_SUCCESS_CASE)
                .formParam('update_service', 'corp-update-password-service')
                .formParam('skip', 'false')

        when:
        def result = setup.when().post('/update')
        then:
        result.then().statusCode(FOUND.value())
                .header(LOCATION, StringContains.containsString("code="))
    }

    def 'validation exception we will receive new update code and try again'() {
        given:
        def setup = given(this.documentationSpec)
                .accept(ContentType.URLENC)
                .filter(document('update_data_by_code_succeed',
                preprocessResponse(prettyPrint()),
                requestParameters(
                        parameterWithName('client_id')
                                .description('relying party identifier'),
                        parameterWithName('login')
                                .description('user login'),
                        parameterWithName('old_password')
                                .description('current user password'),
                        parameterWithName('new_password')
                                .description(''),
                        parameterWithName('reason')
                                .description('what happends'),
                        parameterWithName('update_code')
                                .description('code for update data'),
                        parameterWithName('update_service')
                                .description('name of update service'),
                        parameterWithName('skip')
                                .description('skip update if true')
                )))
                .given()
                .formParam('client_id', CLIENT_NAME)
                .formParam('login', 'XAPA6O')
                .formParam('old_password', 'test1')
                .formParam('new_password', 'test2')
                .formParam('reason', 'password_is_temporary')
                .formParam('update_code', UPDATE_CODE_VALIDATION_EXCEPTION_CASE)
                .formParam('update_service', 'corp-update-password-service')
                .formParam('skip', 'false')

        when:
        def result = setup.when().post('/update')
        then:
        result.then().statusCode(UNPROCESSABLE_ENTITY.value())
        def resp = new JsonSlurper().parseText(result.body.print())
        assert resp.updateCode == '1111-2222-3333-4444'
        assert resp.reason == 'Some validation error as example'
        assert resp.updateService == 'corp-update-password'
    }

    def 'happens some unprocessable exception'() {
        given:
        def setup = given(this.documentationSpec)
                .accept(ContentType.URLENC)
                .filter(document('update_data_by_code_succeed',
                preprocessResponse(prettyPrint()),
                requestParameters(
                        parameterWithName('client_id')
                                .description('relying party identifier'),
                        parameterWithName('login')
                                .description('user login'),
                        parameterWithName('old_password')
                                .description('current user password'),
                        parameterWithName('new_password')
                                .description(''),
                        parameterWithName('reason')
                                .description('what happends'),
                        parameterWithName('update_code')
                                .description('code for update data'),
                        parameterWithName('update_service')
                                .description('name of update service'),
                        parameterWithName('skip')
                                .description('skip update if true')
                )))
                .given()
                .formParam('client_id', CLIENT_NAME)
                .formParam('login', 'XAPA6O')
                .formParam('old_password', 'test1')
                .formParam('new_password', 'test2')
                .formParam('reason', 'password_is_temporary')
                .formParam('update_code', UPDATE_CODE_UNPROСESSABLE_EXCEPTION_CASE)
                .formParam('update_service', 'corp-update-password-service')
                .formParam('skip', 'false')

        when:
        def result = setup.when().post('/update')
        then:
        result.then().statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
    }
}

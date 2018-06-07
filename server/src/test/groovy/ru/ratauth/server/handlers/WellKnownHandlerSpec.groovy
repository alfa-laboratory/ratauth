package ru.ratauth.server.handlers

import com.jayway.restassured.http.ContentType
import org.springframework.restdocs.payload.JsonFieldType
import ru.ratauth.server.BaseDocumentationSpec

import static com.jayway.restassured.RestAssured.given
import static org.springframework.http.HttpStatus.OK
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import static org.springframework.restdocs.restassured.RestAssuredRestDocumentation.document
/**
 * @author tolkv
 * @version 09/11/2016
 */
class WellKnownHandlerSpec extends BaseDocumentationSpec {
  def 'should return openid discovery fields'() {
    given:
    def setup = given(this.documentationSpec)
        .accept(ContentType.JSON)
        .filter(document('should_return_openid_discovery_fields',
        preprocessResponse(prettyPrint()),
        responseFields(
            fieldWithPath('issuer')
                .description('URL using the https scheme with no query or fragment component that the OP asserts as its Issuer Identifier.')
                .type(JsonFieldType.STRING),
            fieldWithPath('authorization_endpoint')
                .description('URL of the OP\'s Authentication and Authorization Endpoint ')
                .optional()
                .type(JsonFieldType.STRING),
            fieldWithPath('token_endpoint')
                .description('URL of the OP\'s OAuth 2.0 Token Endpoint ')
                .optional()
                .type(JsonFieldType.STRING),
            fieldWithPath('check_token_endpoint')
                    .description('URL of the OP\'s OAuth 2.0 Check Token Endpoint ')
                    .optional()
                    .type(JsonFieldType.STRING),
            fieldWithPath('token_endpoint_auth_signing_alg_values_supported')
                .description('JSON array containing a list of the JWS signing algorithms (alg values) supported by the Token Endpoint for the private_key_jwt and client_secret_jwt methods to encode the JWT [JWT]. Servers SHOULD support RS256.')
                .optional()
                .type(JsonFieldType.ARRAY),
            fieldWithPath('registration_endpoint')
                .description('RECOMMENDED. URL of the OP\'s Dynamic Client Registration Endpoin.')
                .type(JsonFieldType.STRING),
            fieldWithPath('userinfo_endpoint')
                .description('RECOMMENDED. URL of the OP\'s UserInfo Endpoint. This URL MUST use the https scheme and MAY contain port, path, and query parameter components.')
                .type(JsonFieldType.STRING),
            fieldWithPath('check_session_iframe')
                .description('URL of an OP endpoint that provides a page to support cross-origin communications for session state information with the RP Client, using the HTML5 postMessage API. The page is loaded from an invisible iframe embedded in an RP page so that it can run in the OP\'s security context. See [OpenID.Session].')
                .optional()
                .type(JsonFieldType.STRING),
            fieldWithPath('end_session_endpoint')
                .description('URL of the OP\'s endpoint that initiates logging out the End-User. See [OpenID.Session].')
                .optional()
                .type(JsonFieldType.STRING),
            fieldWithPath('subject_types_supported')
                .description('JSON array containing a list of the subject identifier types that this server supports. Valid types include pairwise and public.')
                .type(JsonFieldType.ARRAY),
            fieldWithPath('response_types_supported')
                .description('JSON array containing a list of the OAuth 2.0 response_type values that this server supports. The server MUST support the code, id_token, and the token id_token response type values.')
                .type(JsonFieldType.ARRAY),
            fieldWithPath('jwks_uri')
                .description('URL of the OP\'s JSON Web Key Set [JWK] document. This contains the signing key(s) the Client uses to validate signatures from the OP. The JWK Set MAY also contain the Server\'s encryption key(s), which are used by Clients to encrypt requests to the Server. When both signing and encryption keys are made available, a use (Key Use) parameter value is REQUIRED for all keys in the document to indicate each key\'s intended usage.')
                .type(JsonFieldType.STRING),
            fieldWithPath('scopes_supported')
                .description('RECOMMENDED. JSON array containing a list of the OAuth 2.0 response_type values that this server supports. The server MUST support the code, id_token, and the token id_token response type values.')
                .optional()
                .type(JsonFieldType.ARRAY),
            fieldWithPath('claims_supported')
                .description('RECOMMENDED. JSON array containing a list of the Claim Names of the Claims that the OpenID Provider MAY be able to supply values for. Note that for privacy or other reasons, this might not be an exhaustive list.')
                .optional()
                .type(JsonFieldType.ARRAY),
            fieldWithPath('afp_endpoint')
                .description('String containing URL for non-clients.')
                .optional()
                .type(JsonFieldType.STRING),

        )
    ))
    when:
    def result = setup.when().post('.well-known/openid-configuration')

    then:
    result.then()
        .statusCode(OK.value())
  }
}

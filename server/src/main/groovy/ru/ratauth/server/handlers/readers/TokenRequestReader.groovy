package ru.ratauth.server.handlers.readers

import groovy.transform.CompileStatic
import ratpack.form.Form
import ratpack.http.Headers
import ru.ratauth.exception.AuthorizationException
import ru.ratauth.interaction.AuthzResponseType
import ru.ratauth.interaction.CheckTokenRequest
import ru.ratauth.interaction.GrantType
import ru.ratauth.interaction.TokenRequest

import static ru.ratauth.server.handlers.readers.RequestUtil.*

/**
 * @author djassan
 * @since 06/11/15
 */
@CompileStatic
class TokenRequestReader {
  private static final String RESPONSE_TYPE = "response_type"
  private static final String SCOPE = "scope"
  private static final String GRANT_TYPE = "grant_type"
  private static final String CODE = "code"
  private static final String TOKEN = "token"
  private static final String REFRESH_TOKEN = "refresh_token"
  private static final String CLIENT_ID = "client_id"
  private static final Set<String> BASE_FIELDS = [RESPONSE_TYPE, SCOPE, GRANT_TYPE, CODE, TOKEN, REFRESH_TOKEN] as Set

  static TokenRequest readTokenRequest(Form form, Headers headers) {
    def auth = extractAuth(headers)
    GrantType grantType = GrantType.valueOf(extractField(form, GRANT_TYPE, true).toUpperCase())
    def builder = TokenRequest.builder()
        .responseType(AuthzResponseType.valueOf(extractField(form, RESPONSE_TYPE, true).toUpperCase()))
        .grantType(grantType)
        .clientId(auth[0])
        .clientSecret(auth[1])
    switch(grantType){
      case GrantType.AUTHORIZATION_CODE: builder.authzCode(extractField(form, CODE, true))
        break
      case GrantType.AUTHENTICATION_TOKEN: builder.scopes(extractField(form, SCOPE, true)?.split(" ")?.toList()) //WATCH IT! There is no break here.
      case GrantType.REFRESH_TOKEN: builder.refreshToken(extractField(form, REFRESH_TOKEN, true))
        break;
      default: throw new AuthorizationException("Grant type is not supported");
    }
    builder.authData(extractRest(form, BASE_FIELDS))
    builder.build()
  }

  static CheckTokenRequest readCheckTokenRequest(Form form, Headers headers) {
    CheckTokenRequest.CheckTokenRequestBuilder builder = CheckTokenRequest.builder().token(extractField(form, TOKEN, true));
    def auth = extractAuth(headers)
    builder.clientId(auth[0]).clientSecret(auth[1]);
    builder.externalClientId(extractField(form, CLIENT_ID, false))
    builder.build()
  }
}
package ru.ratauth.server.handlers.readers

import groovy.transform.CompileStatic
import ratpack.http.Headers
import ratpack.util.MultiValueMap
import ru.ratauth.interaction.AuthzRequest
import ru.ratauth.interaction.AuthzResponseType
import ru.ratauth.interaction.GrantType
import ru.ratauth.server.services.log.ActionLogger
import ru.ratauth.server.services.log.AuthAction

import static ru.ratauth.server.handlers.readers.RequestUtil.*

/**
 * @author djassan
 * @since 06/11/15
 */
@CompileStatic
class AuthzRequestReader {
  public static final String FIELD_SPLITTER = ' '
  private static final String RESPONSE_TYPE = 'response_type'
  private static final String GRANT_TYPE = 'grant_type'
  private static final String CLIENT_ID = 'client_id'
  private static final String SCOPE = 'scope'
  private static final String REDIRECT_URI = 'redirect_uri'
  private static final String REFRESH_TOKEN = 'refresh_token'
  private static final Set<String> BASE_FIELDS = [
          RESPONSE_TYPE,
          CLIENT_ID,
          SCOPE,
          REDIRECT_URI,
          REFRESH_TOKEN,
          GRANT_TYPE
  ] as Set

  static AuthzRequest readAuthzRequest(MultiValueMap<String, String> params, Headers headers) {
    AuthzResponseType responseType = extractEnumField(params, RESPONSE_TYPE, true, AuthzResponseType)
    GrantType grantType = extractEnumField(params, GRANT_TYPE, false, GrantType)
    AuthAction authAction
    def builder = AuthzRequest.builder()
        .responseType(responseType)
        .redirectURI(extractField(params, REDIRECT_URI, false))

    if (GrantType.AUTHENTICATION_TOKEN == grantType) {
      if (responseType == AuthzResponseType.TOKEN) {
        throw new ReadRequestException(ReadRequestException.ID.WRONG_REQUEST, 'Response for that grant_type could not be Token')
      }
      authAction = AuthAction.CROSS_AUTHORIZATION
      def auth = extractAuth(headers)
      builder.clientId(auth[0])
          .clientSecret(auth[1])
          .refreshToken(extractField(params, REFRESH_TOKEN, true))
          .externalClientId(extractField(params, CLIENT_ID, true))
          .grantType(grantType)
          .scopes(extractField(params, SCOPE, true).split(FIELD_SPLITTER).toList())
    } else if (responseType == AuthzResponseType.TOKEN) {
      authAction = AuthAction.AUTHORIZATION
      def auth = extractAuth(headers)
      builder.clientId(auth[0])
          .clientSecret(auth[1])
      def scope = extractField(params, SCOPE, false)?.split(FIELD_SPLITTER)?.toList()
      if (scope) {
        builder.scopes(scope)
      }
    } else {
      authAction = AuthAction.AUTHORIZATION
      builder.clientId(extractField(params, CLIENT_ID, true))
      def scope = extractField(params, SCOPE, false)?.split(FIELD_SPLITTER)?.toList()
      if (scope) {
        builder.scopes(scope)
      }
    }
    builder.authData(extractRest(params, BASE_FIELDS))
    def request = builder.build()
    ActionLogger.addBaseRequestInfo(request.clientId, authAction)
    request
  }

  static String readClientId(MultiValueMap<String, String> params) {
    extractField(params, CLIENT_ID, true)
  }
}

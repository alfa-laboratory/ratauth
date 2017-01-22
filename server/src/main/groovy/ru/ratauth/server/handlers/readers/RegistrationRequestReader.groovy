package ru.ratauth.server.handlers.readers

import groovy.transform.CompileStatic
import ratpack.http.Headers
import ratpack.util.MultiValueMap
import ru.ratauth.interaction.AuthzResponseType
import ru.ratauth.interaction.GrantType
import ru.ratauth.interaction.RegistrationRequest
import ru.ratauth.server.services.log.ActionLogger
import ru.ratauth.server.services.log.AuthAction

import static ru.ratauth.server.handlers.readers.RequestUtil.*

/**
 * @author mgorelikov
 * @since 29/01/16
 */
@CompileStatic
class RegistrationRequestReader {
  private static final String CLIENT_ID = 'client_id'
  private static final String GRANT_TYPE = 'grant_type'
  private static final String RESPONSE_TYPE = 'response_type'
  private static final String SCOPE = 'scope'
  private static final String ACR_VALUES = 'acr_values'
  private static final Set BASE_FIELDS = [
      CLIENT_ID,
      GRANT_TYPE,
      RESPONSE_TYPE,
      SCOPE
  ] as Set

  static RegistrationRequest readRegistrationRequest(MultiValueMap<String, String> form) {
    GrantType grantType = extractEnumField(form, GRANT_TYPE, false, GrantType)
    def request = RegistrationRequest.builder()
        .grantType(grantType)
        .responseTypes( extractEnumFields(form, RESPONSE_TYPE, ' ', true, AuthzResponseType))
        .data(extractRest(form, BASE_FIELDS))
        .clientId(extractField(form, CLIENT_ID, true))
        .scopes(extractField(form, SCOPE, true).split(' ').toList())
        .acrValues(extractField(form, ACR_VALUES, false)?.split(' ')?.toList())
        .build()
    ActionLogger.addBaseRequestInfo(request.clientId, AuthAction.REGISTRATION)
    request
  }
}
package ru.ratauth.server.handlers.readers

import ratpack.form.Form
import ratpack.http.Headers
import ratpack.http.Request
import ratpack.util.MultiValueMap
import ru.ratauth.interaction.AuthzResponseType
import ru.ratauth.interaction.CheckTokenRequest
import ru.ratauth.interaction.TokenRequest
import ru.ratauth.server.utils.StringUtils

/**
 * @author djassan
 * @since 06/11/15
 */
class TokenRequestReader {
  private static final String RESPONSE_TYPE = "response_type"
  private static final String USERNAME = "username"
  private static final String PASSWORD = "password"
  private static final String CODE = "code"
  private static final String TOKEN = "token"
  private static final String AUTHORIZATION = "Authorization"


  static TokenRequest readTokenRequest(Form form, Headers headers) {
    def auth = extractAuth(headers)
    TokenRequest.builder()
      .responseType(AuthzResponseType.valueOf(extractField(form, RESPONSE_TYPE).toUpperCase()))
      .username(extractField(form, USERNAME))
      .password(extractField(form,PASSWORD))
      .code(extractField(form,CODE))
      .clientId(auth[0])
      .clientSecret(auth[1])
      .build()


  }

  static CheckTokenRequest readCheckTokenRequest(Form form, Headers headers) {
    CheckTokenRequest.builder().token(extractField(form, TOKEN)).build();
  }

  private static String extractField(MultiValueMap<String, String> params, String name) {
    String value = params.get(name)
    if(StringUtils.isBlank(value))
      throw new ReadRequestException(name)
    value
  }

  private static String [] extractAuth(Headers headers) {
    def authHeader = headers?.get(AUTHORIZATION)
    if(!authHeader)
      throw new ReadRequestException(AUTHORIZATION)
    def encodedValue = authHeader.split(" ")[1]
    def decodedValue = new String(encodedValue.decodeBase64())?.split(":")
    // do some sort of validation here
    if (decodedValue[0] && decodedValue[1]) {
      decodedValue
    } else {
      throw new ReadRequestException(AUTHORIZATION)
    }
  }

}
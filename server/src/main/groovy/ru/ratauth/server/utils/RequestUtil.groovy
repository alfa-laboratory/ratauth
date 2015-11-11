package ru.ratauth.server.utils

import ratpack.http.Headers
import ratpack.util.MultiValueMap
import ru.ratauth.server.handlers.readers.ReadRequestException
import ru.ratauth.utils.StringUtils

/**
 * @author mgorelikov
 * @since 11/11/15
 */
class RequestUtil {
  private static final String AUTHORIZATION = "Authorization"

  public static String extractField(MultiValueMap<String, String> params, String name, boolean required) {
    String value = params.get(name)
    if(StringUtils.isBlank(value) && required)
      throw new ReadRequestException(name)
    value
  }

  public static String [] extractAuth(Headers headers) {
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

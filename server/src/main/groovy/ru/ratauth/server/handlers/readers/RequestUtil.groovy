package ru.ratauth.server.handlers.readers

import groovy.transform.CompileStatic
import ratpack.form.Form
import ratpack.http.Headers
import ratpack.util.MultiValueMap
import ratpack.util.internal.ImmutableDelegatingMultiValueMap
import ru.ratauth.interaction.GrantType
import ru.ratauth.utils.StringUtils

/**
 * @author mgorelikov
 * @since 11/11/15
 */
@CompileStatic
class RequestUtil {
  private static final String AUTHORIZATION = "Authorization"

  /**
   * Extracts field according to it's name
   * @param params input map
   * @param name name of parameter that must be exctracted
   * @param required if true and parameter with name was not found throws ReadRequestException
   * @return extracted param value
   */
  public static String extractField(MultiValueMap<String, String> params, String name, boolean required) {
    String value = params.get(name)
    if(StringUtils.isBlank(value) && required)
      throw new ReadRequestException(name)
    value
  }

  /**
   * Extracts field according to it's name and return enum corresponding for it's value
   * @param params input map
   * @param name name of parameter that must be exctracted
   * @param required if true and parameter with name was not found throws ReadRequestException
   * @return extracted param value
   */
  public static <T extends Enum>  T extractEnumField(MultiValueMap<String, String> params, String name, boolean required, Class<T> enumType) {
    String value = extractField(params, name, required)
    if(!StringUtils.isBlank(value))
      Enum.valueOf(enumType, value.toUpperCase())
    else
      null
  }

  /**
   * Extracts field with names that are not given in excludedNames
   * @param params input map
   * @param excludedNames names that must be excluded from result
   * @return resulted map of params
   */
  public static Map<String,String> extractRest(MultiValueMap<String, String> params, Set<String> excludedNames) {
    //since duplicate must be removed we won't use clone method
    Map<String,String> result = new HashMap<>()
    params.entrySet().stream().filter{ !excludedNames.contains(it.key) } forEach{ result.put(it.key, it.value) }
    return result
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

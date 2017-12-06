package ru.ratauth.server.handlers.readers

import groovy.transform.CompileStatic
import ratpack.http.Headers
import ru.ratauth.utils.StringUtils

import java.nio.charset.Charset

/**
 * @author mgorelikov
 * @since 11/11/15
 */
@CompileStatic
class RequestUtil {
  private static final String AUTHORIZATION = "Authorization"

  /**
   * Extracts field according to it's name and remove it from map
   * @param params input map
   * @param name name of parameter that must be exctracted
   * @param required if true and parameter with name was not found throws ReadRequestException
   * @return extracted param value
   */
  static String removeField(Map<String, String> params, String name, boolean required) {
    String value = params.remove(name)
    if (StringUtils.isBlank(value) && required) {
      throw new ReadRequestException(ReadRequestException.ID.FIELD_MISSED, name)
    }
    return value
  }

  /**
   * Extracts field according to it's name
   * @param params input map
   * @param name name of parameter that must be exctracted
   * @param required if true and parameter with name was not found throws ReadRequestException
   * @return extracted param value
   */
  static String extractField(Map<String, String> params, String name, boolean required) {
    String value = params.get(name)
    if (StringUtils.isBlank(value) && required) {
      throw new ReadRequestException(ReadRequestException.ID.FIELD_MISSED, name)
    }
    value
  }

  /**
   * Extracts field according to it's name and return enum corresponding for it's value
   * @param params input map
   * @param name name of parameter that must be exctracted
   * @param required if true and parameter with name was not found throws ReadRequestException
   * @return extracted param value
   */
  static <T extends Enum> T extractEnumField(Map<String, String> params, String name, boolean required, Class<T> enumType) {
    String value = extractField(params, name, required)
    if (!StringUtils.isBlank(value)) {
      return Enum.valueOf(enumType, value.toUpperCase())
    }
    null
  }

  /**
   * Extracts field according to it's name and return enum corresponding for it's value
   * @param params input map
   * @param name name of parameter that must be exctracted
   * @param required if true and parameter with name was not found throws ReadRequestException
   * @return extracted param value
   */
  static <T extends Enum> List<T> extractEnumFields(Map<String, String> params, String name, String splitter,
                                                    boolean required, Class<T> enumType) {
    List<T> result = extractField(params, name, required)?.split(splitter)
        ?.findAll { !StringUtils.isBlank(it) }
        ?.collect { Enum.valueOf(enumType, it.toUpperCase()) }
    if (required && result.isEmpty()) {
      throw new ReadRequestException(ReadRequestException.ID.FIELD_MISSED, name)
    }
    result
  }

  /**
   * Extracts field with names that are not given in excludedNames
   * @param params input map
   * @param excludedNames names that must be excluded from result
   * @return resulted map of params
   */
  static Map<String, String> extractRest(Map<String, String> params, Set<String> excludedNames = []) {
    //since duplicate must be removed we won't use clone method
    Map<String, String> result = [:]
    params.entrySet().stream().filter { !excludedNames.contains(it.key) } forEach { result.put(it.key, it.value) }
    return result
  }

  static String[] extractAuth(Headers headers) {
    def authHeader = headers?.get(AUTHORIZATION)
    if (!authHeader) {
      throw new ReadRequestException(ReadRequestException.ID.FIELD_MISSED, AUTHORIZATION)
    }
    def encodedValue = authHeader.split(" ")[1]
    def decodedValue = new String(encodedValue.decodeBase64(), Charset.forName("UTF-8"))?.split(":")
    // do some sort of validation here
    if (decodedValue[0] && decodedValue[1]) {
      decodedValue
    } else {
      throw new ReadRequestException(ReadRequestException.ID.FIELD_MISSED, AUTHORIZATION)
    }
  }
}

package ru.ratauth.server.utils;

/**
 * @author mgorelikov
 * @since 02/11/15
 */
public class StringUtils {
  /**
   * <p>Checks if a String is whitespace, empty ("") or null.</p>
   * <p>
   * <pre>
   * StringUtils.isBlank(null)      = true
   * StringUtils.isBlank("")        = true
   * StringUtils.isBlank(" ")       = true
   * StringUtils.isBlank("bob")     = false
   * StringUtils.isBlank("  bob  ") = false
   * </pre>
   *
   * @param str the String to check, may be null
   * @return <code>true</code> if the String is null, empty or whitespace
   * @since 2.0
   */
  public static boolean isBlank(String str) {
    int strLen;
    if (str == null || (strLen = str.length()) == 0) {
      return true;
    }
    for (int i = 0; i < strLen; i++) {
      if ((!Character.isWhitespace(str.charAt(i)))) {
        return false;
      }
    }
    return true;
  }
}

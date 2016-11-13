package ru.ratauth.utils;

import lombok.SneakyThrows;
import ru.ratauth.exception.AuthorizationException;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;

/**
 * @author mgorelikov
 * @since 13/11/16
 */
public class URIUtils {

  // TODO maybe need to think about param duplications
  public static String appendQuery(String uri, String query) {
    StringBuilder builder = new StringBuilder(uri);
    if (!uri.contains("?")) {
      builder.append("?").append(query);
    } else {
      builder.append("&").append(query);
    }
    return builder.toString();
  }

  // TODO must be improved
  @SneakyThrows
  public static boolean compareHosts(String address, List<String> referenceURIs) {
    try {
      if (StringUtils.isBlank(address))
        return false;
      final URI uri = URI.create(address);
      URL serverURL = new URL(uri.getScheme(),      // http
          uri.getHost(),  // host
          uri.getPort(),  // port
          "");
      return referenceURIs.contains(serverURL.toString());
    } catch (Exception e) {
      throw new AuthorizationException(AuthorizationException.ID.REDIRECT_NOT_CORRECT);
    }
  }
}

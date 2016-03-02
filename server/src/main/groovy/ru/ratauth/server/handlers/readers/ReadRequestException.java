package ru.ratauth.server.handlers.readers;

import ru.ratauth.exception.BaseAuthServerException;

/**
 * @author djassan
 * @since 06/11/15
 */
public class ReadRequestException extends BaseAuthServerException {

  private static final String BASE_TEXT ="Required field not found: ";
  public ReadRequestException() {
  }


  public ReadRequestException(Throwable cause) {
    super(cause);
  }

  public ReadRequestException(String fieldName) {
    super(BASE_TEXT + fieldName);
  }

  public ReadRequestException(String fieldName, Throwable cause) {
    super(BASE_TEXT + fieldName, cause);
  }
}

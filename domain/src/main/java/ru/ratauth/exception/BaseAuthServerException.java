package ru.ratauth.exception;

/**
 * @author mgorelikov
 * @since 23/02/16
 */
public class BaseAuthServerException extends RuntimeException {
  public BaseAuthServerException() {
  }

  public BaseAuthServerException(String message) {
    super(message);
  }

  public BaseAuthServerException(String message, Throwable cause) {
    super(message, cause);
  }

  public BaseAuthServerException(Throwable cause) {
    super(cause);
  }
}

package ru.ratauth.exception;

/**
 * @author mgorelikov
 * @since 10/11/15
 */
public class AuthorizationException extends BaseAuthServerException{

  public AuthorizationException() {
  }

  public AuthorizationException(String message) {
    super(message);
  }

  public AuthorizationException(String message, Throwable cause) {
    super(message, cause);
  }

  public AuthorizationException(Throwable cause) {
    super(cause);
  }
}

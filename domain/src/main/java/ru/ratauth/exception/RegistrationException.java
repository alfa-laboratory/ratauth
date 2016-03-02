package ru.ratauth.exception;

/**
 * @author mgorelikov
 * @since 10/11/15
 */
public class RegistrationException extends BaseAuthServerException{

  public RegistrationException() {
  }

  public RegistrationException(String message) {
    super(message);
  }

  public RegistrationException(String message, Throwable cause) {
    super(message, cause);
  }

  public RegistrationException(Throwable cause) {
    super(cause);
  }
}

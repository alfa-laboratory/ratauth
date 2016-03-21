package ru.ratauth.exception;

/**
 * @author mgorelikov
 * @since 21/03/16
 */
public class InternalLogicException extends BaseAuthServerException {

  public InternalLogicException() {
  }

  public InternalLogicException(String message) {
    super(message);
  }

  public InternalLogicException(String message, Throwable cause) {
    super(message, cause);
  }

  public InternalLogicException(Throwable cause) {
    super(cause);
  }
}

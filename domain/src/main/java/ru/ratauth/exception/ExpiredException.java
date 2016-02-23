package ru.ratauth.exception;

/**
 * @author mgorelikov
 * @since 12/11/15
 * Entity expired exception
 */
public class ExpiredException extends BaseAuthServerException {
  public ExpiredException() {
  }

  public ExpiredException(String message) {
    super(message);
  }

  public ExpiredException(String message, Throwable cause) {
    super(message, cause);
  }

  public ExpiredException(Throwable cause) {
    super(cause);
  }
}

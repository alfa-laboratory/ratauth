package ru.ratauth.exception;

/**
 * @author mgorelikov
 * @since 12/11/15
 * Entity expired exception
 */
public class ExpiredException extends RuntimeException {
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

  public ExpiredException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}

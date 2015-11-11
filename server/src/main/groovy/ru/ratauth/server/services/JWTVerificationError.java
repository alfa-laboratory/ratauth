package ru.ratauth.server.services;

/**
 * @author mgorelikov
 * @since 11/11/15
 */
public class JWTVerificationError extends RuntimeException {

  public JWTVerificationError() {
  }

  public JWTVerificationError(String message) {
    super(message);
  }

  public JWTVerificationError(String message, Throwable cause) {
    super(message, cause);
  }

  public JWTVerificationError(Throwable cause) {
    super(cause);
  }

  public JWTVerificationError(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}

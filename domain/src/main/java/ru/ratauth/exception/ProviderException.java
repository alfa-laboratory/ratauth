package ru.ratauth.exception;

/**
 * @author mgorelikov
 * @since 15/03/16
 */
public class ProviderException extends BaseAuthServerException {
  public ProviderException() {
  }

  public ProviderException(String message) {
    super(message);
  }

  public ProviderException(String message, Throwable cause) {
    super(message, cause);
  }

  public ProviderException(Throwable cause) {
    super(cause);
  }
}

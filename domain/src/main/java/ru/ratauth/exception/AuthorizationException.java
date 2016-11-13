package ru.ratauth.exception;

/**
 * @author mgorelikov
 * @since 10/11/15
 */
public class AuthorizationException extends BaseAuthServerException implements IdentifiedException {
  private final String id;

  public AuthorizationException(String id) {
    this.id = id;
  }

  public AuthorizationException(ID id) {
    super(id.getBaseText());
    this.id = id.name();
  }

  public AuthorizationException(String id, String message) {
    super(message);
    this.id = id;
  }

  public AuthorizationException(String id, String message, Throwable cause) {
    super(message, cause);
    this.id = id;
  }

  public AuthorizationException(String id, Throwable cause) {
    super(cause);
    this.id = id;
  }

  @Override
  public String getTypeId() {
    return Type.AUTHORIZATION.name();
  }

  @Override
  public String getId() {
    return id;
  }

  public enum ID {
    CLIENT_NOT_FOUND("Client not found"),
    CREDENTIALS_WRONG("Credentials are wrong"),
    TOKEN_NOT_FOUND("Token not found"),
    SESSION_NOT_FOUND("Session not found"),
    SESSION_BLOCKED("Session is blocked"),
    REDIRECT_NOT_CORRECT("Redirect url is not correct"),
    INVALID_GRANT_TYPE("Invalid grant type");

    private final String baseText;

    ID(String baseText) {
      this.baseText = baseText;
    }

    public String getBaseText() {
      return baseText;
    }
  }
}

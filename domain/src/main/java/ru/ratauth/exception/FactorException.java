package ru.ratauth.exception;

/**
 * @author mgorelikov
 * @since 10/11/15
 */
public class FactorException extends BaseAuthServerException implements IdentifiedException {
  private final String id;

  public FactorException(String id) {
    this.id = id;
  }

  public FactorException(ID id) {
    super(id.getBaseText());
    this.id = id.name();
  }

  public FactorException(String id, String message) {
    super(message);
    this.id = id;
  }

  public FactorException(String id, String message, Throwable cause) {
    super(message, cause);
    this.id = id;
  }

  public FactorException(String id, Throwable cause) {
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
    SESSION_BLOCKED("Session is blocked");

    private final String baseText;

    ID(String baseText) {
      this.baseText = baseText;
    }

    public String getBaseText() {
      return baseText;
    }
  }
}

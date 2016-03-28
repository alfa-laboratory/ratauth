package ru.ratauth.exception;

/**
 * @author mgorelikov
 * @since 12/11/15
 * Entity expired exception
 */
public class ExpiredException extends BaseAuthServerException implements IdentifiedException {
  private final String id;

  public ExpiredException(ID id) {
    super(id.baseText);
    this.id = id.name();
  }

  public ExpiredException(String id) {
    this.id = id;
  }

  public ExpiredException(String id, String message) {
    super(message);
    this.id = id;
  }

  public ExpiredException(String id, String message, Throwable cause) {
    super(message, cause);
    this.id = id;
  }

  public ExpiredException(String id, Throwable cause) {
    super(cause);
    this.id = id;
  }

  @Override
  public String getTypeId() {
    return Type.EXPIRED.name();
  }

  @Override
  public String getId() {
    return id;
  }

  public enum ID {
    AUTH_CODE_EXPIRED("Auth code has expired"),
    TOKEN_EXPIRED("Token has expired"),
    REFRESH_TOKEN_EXPIRED("Refresh token has expired"),
    SESSION_EXPIRED("Session has expired");

    private final String baseText;

    ID(String baseText) {
      this.baseText = baseText;
    }

    public String getBaseText() {
      return baseText;
    }
  }
}

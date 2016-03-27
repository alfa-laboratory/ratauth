package ru.ratauth.exception;

/**
 * @author mgorelikov
 * @since 10/11/15
 */
public class RegistrationException extends BaseAuthServerException implements IdentifiedException {
  private final String id;

  public RegistrationException(ID id) {
    super(id.baseText);
    this.id = id.name();
  }

  public RegistrationException(String id) {
    this.id = id;
  }

  public RegistrationException(String id, String message) {
    super(message);
    this.id = id;
  }

  public RegistrationException(String id, String message, Throwable cause) {
    super(message, cause);
    this.id = id;
  }

  public RegistrationException(String id, Throwable cause) {
    super(cause);
    this.id = id;
  }

  @Override
  public String getTypeId() {
    return Type.REGISTRATION.name();
  }

  @Override
  public String getId() {
    return id;
  }

  public enum ID {
    CLIENT_NOT_FOUND("Client not found"),
    CREDENTIALS_WRONG("Credentials are wrong");

    private final String baseText;

    ID(String baseText) {
      this.baseText = baseText;
    }

    public String getBaseText() {
      return baseText;
    }
  }
}

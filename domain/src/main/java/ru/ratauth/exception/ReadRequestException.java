package ru.ratauth.exception;

import ru.ratauth.exception.BaseAuthServerException;
import ru.ratauth.exception.IdentifiedException;

/**
 * @author djassan
 * @since 06/11/15
 */
public class ReadRequestException extends BaseAuthServerException implements IdentifiedException {
  private static final String BASE_TEXT = "Required field not found: ";
  private static final String TYPE_ID = "REQUEST_READER";

  private final String id;
  private final String field;

  public ReadRequestException(String id) {
    this.id = id;
    this.field = null;
  }

  public ReadRequestException(ID id, String fieldName) {
    super(id.baseText + fieldName);
    this.id = id.name();
    this.field = fieldName;
  }

  public ReadRequestException(ID id, String fieldName, Throwable cause) {
    super(id.baseText + fieldName, cause);
    this.id = id.name();
    this.field = fieldName;
  }

  public ReadRequestException(ID id, Throwable cause) {
    super(cause);
    this.id = id.name();
    this.field = null;
  }

  @Override
  public String getTypeId() {
    return TYPE_ID;
  }

  @Override
  public String getId() {
    return this.id;
  }

  public String getField() {
    return this.field;
  }

  public enum ID {
    FIELD_MISSED("Required field not found: "),
    WRONG_REQUEST("Wrong request: ");

    private final String baseText;

    ID(String baseText) {
      this.baseText = baseText;
    }

    public String getBaseText() {
      return baseText;
    }
  }
}

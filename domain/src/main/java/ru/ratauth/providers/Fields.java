package ru.ratauth.providers;

import ru.ratauth.providers.auth.dto.BaseAuthFields;

/**
 * Base fields for providers userInfo response
 * @author mgorelikov
 * @since 05/02/16
 */
public enum Fields {
  USERNAME(BaseAuthFields.USERNAME.val()),
  USER_ID(BaseAuthFields.USER_ID.val());
  private String value;

  Fields(String value) {
    this.value = value;
  }

  public String val() {
    return value;
  }

  @Override
  public String toString() {
    return value;
  }
}

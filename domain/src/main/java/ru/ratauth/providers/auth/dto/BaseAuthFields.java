package ru.ratauth.providers.auth.dto;

/**
 * @author mgorelikov
 * @since 26/01/16
 * Just basic fields
 */
public enum BaseAuthFields {
  USERNAME,
  USER_ID,//constants for standard user info
  PASSWORD,
  CODE;

  public String val() {
    return super.name().toLowerCase();
  }
}

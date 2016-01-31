package ru.ratauth.providers.auth.dto;

/**
 * @author mgorelikov
 * @since 26/01/16
 * Just basic fields
 */
public enum BaseAuthFields {
  LOGIN,
  PASSWORD,
  AUTHCODE;

  public String val() {
    return super.name().toLowerCase();
  }
}

package ru.ratauth.providers.registrations.dto;

/**
 * @author mgorelikov
 * @since 28/01/16
 */
public enum BaseRegFields {
  LOGIN,
  REGCODE;

  public String val() {
    return super.name().toLowerCase();
  }
}

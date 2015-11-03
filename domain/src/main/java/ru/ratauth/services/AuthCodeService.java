package ru.ratauth.services;

import ru.ratauth.entities.AuthCode;

/**
 * @author mgorelikov
 * @since 02/11/15
 */
public interface AuthCodeService {
  AuthCode save(AuthCode code);

  /**
   *
   * @param code
   * @return code entity if it was found, otherwise must return null
   */
  AuthCode get(String code);
}

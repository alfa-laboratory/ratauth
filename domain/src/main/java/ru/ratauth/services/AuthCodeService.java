package ru.ratauth.services;

import ru.ratauth.entities.AuthCode;

/**
 * @author mgorelikov
 * @since 02/11/15
 */
public interface AuthCodeService {
  AuthCode save(AuthCode code);
}

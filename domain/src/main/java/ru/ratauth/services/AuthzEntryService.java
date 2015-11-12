package ru.ratauth.services;

import ru.ratauth.entities.AuthzEntry;
import rx.Observable;

import java.util.Date;

/**
 * @author mgorelikov
 * @since 02/11/15
 *
 * Persistence layer
 * Async token service
 */
public interface AuthzEntryService {
  Observable<AuthzEntry> save(AuthzEntry code);

  /**
   * Loads AuthzEntry by code with expiration date check
   * @param code code value
   * @param now current date
   * @return Observable of single AuthzEntry or Observable.empty if code not found or Observable.error if code has expired
   */
  Observable<AuthzEntry> getByValidCode(String code, Date now);

  /**
   * Loads AuthzEntry by refreshToken with expiration date check
   * @param token refreshToken value
   * @param now current date
   * @return Observable of single AuthzEntry or Observable.empty if refresh token not found or Observable.error if refresh token has expired
   */
  Observable<AuthzEntry> getByValidRefreshToken(String token, Date now);

  /**
   * Loads AuthzEntry by refreshToken with expiration date check. AuthzEntry must be loaded with only one token entity
   * @param token access token value
   * @param now current date
   * @return Observable of single AuthzEntry or Observable.empty if token not found or Observable.error if token has expired
   */
  Observable<AuthzEntry> getByValidToken(String token, Date now);
}

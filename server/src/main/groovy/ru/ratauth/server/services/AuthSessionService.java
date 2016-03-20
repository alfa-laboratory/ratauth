package ru.ratauth.server.services;

import ru.ratauth.entities.RelyingParty;
import ru.ratauth.entities.Session;
import rx.Observable;

import java.util.Date;
import java.util.Map;
import java.util.Set;

/**
 * @author mgorelikov
 * @since 16/02/16
 */
public interface AuthSessionService {
  /**
   * Creates and saves empty session with authEntry for input relyingParty
   * @param relyingParty
   * @param userInfo info got from provider
   * @param scopes
   * @param redirectUrl
   * @return session entity
   */
  Observable<Session> initSession(RelyingParty relyingParty, Map<String, Object> userInfo, Set<String> scopes, String redirectUrl);

  /**
   * Creates and saves session with authEntry and token granted for input relyingParty
   * @param relyingParty
   * @param userInfo info got from provider
   * @param scopes
   * @param redirectUrl
   * @return session entity
   */
  Observable<Session> createSession(RelyingParty relyingParty, Map<String, Object> userInfo, Set<String> scopes, String redirectUrl);

  /**
   * Adds token to session. Could be used in refresh token flow
   * @param session
   * @param relyingParty
   * @return
   */
  Observable<Boolean> addToken(Session session, RelyingParty relyingParty);

  /**
   * Creates new entry for relyingParty within existing session.
   * Could be used in cross-authorization flow
   * @param session
   * @param relyingParty
   * @param scopes
   * @param redirectUrl
   * @return
   */
  Observable<Session> addEntry(Session session, RelyingParty relyingParty, Set<String> scopes, String redirectUrl);

  /**
   * Loads AuthEntry by code with expiration date check. Session must be loaded with only one entry
   *
   * @param code code value
   * @param now  current date
   * @return Observable of single AuthEntry or Observable.empty if code not found or Observable.error if code has expired
   */
  Observable<Session> getByValidCode(String code, Date now);

  /**
   * Loads AuthEntry by refreshToken with expiration date check. Session must be loaded with only one entry
   *
   * @param token refreshToken value
   * @param now   current date
   * @return Observable of single AuthEntry or Observable.empty if refresh token not found or Observable.error if refresh token has expired
   */
  Observable<Session> getByValidRefreshToken(String token, Date now);

  /**
   * Loads Session by refreshToken with expiration date check. Session must be loaded with only one entry and token entity
   *
   * @param token access token value
   * @param now   current date
   * @return Observable of single AuthEntry with single token or Observable.empty if token not found or Observable.error if token has expired
   */
  Observable<Session> getByValidToken(String token, Date now);
}

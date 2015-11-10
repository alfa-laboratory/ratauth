package ru.ratauth.services;

import ru.ratauth.entities.Token;
import rx.Observable;

/**
 * @author mgorelikov
 * @since 02/11/15
 *
 * Persistence layer
 * Async token service
 */
public interface TokenService {
  Observable<Token> save(Token token);

  /**
   * Loads token from entity layer by token value
   * @param token entity identifier
   * @return loaded token entity
   */
  Observable<Token> get(String token);
}

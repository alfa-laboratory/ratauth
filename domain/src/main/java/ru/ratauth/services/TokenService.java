package ru.ratauth.services;

import ru.ratauth.entities.Token;

/**
 * @author mgorelikov
 * @since 02/11/15
 */
public interface TokenService {
  Token save(Token token);
  Token get(String token);
}

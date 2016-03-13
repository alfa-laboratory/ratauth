package ru.ratauth.server.services;

import ru.ratauth.entities.AuthClient;
import ru.ratauth.entities.AuthEntry;
import ru.ratauth.entities.Session;
import ru.ratauth.entities.TokenCache;
import ru.ratauth.providers.Fields;
import rx.Observable;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author mgorelikov
 * @since 19/02/16
 */
public interface TokenCacheService {
  /**
   * Tries to load token from cache, in case it was not found creates new jwt from session.userInfo signed by authClient.secret
   * @param session use session
   * @param authClient authClient
   * @param authEntry target entry
   * @return cached jwt token
   */
  Observable<TokenCache> getToken(Session session, AuthClient authClient, AuthEntry authEntry);

  /**
   * Just extract from scopes like 'some_resource_server.read,some_resource_server.write' audience 'some_resource_server'
   * @param scopes
   * @return audience
   */
  default Set<String> extractAudience(Set<String> scopes) {
    return scopes.stream().map(scope -> scope.split("\\.")[0]).collect(Collectors.toSet());
  }
}

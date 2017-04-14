package ru.ratauth.inmemory.ip.services;

import ru.ratauth.entities.TokenCache;
import ru.ratauth.services.TokenCacheService;
import rx.Observable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.util.Objects.*;

public class InMemoryTokenCacheService implements TokenCacheService {

  private final List<TokenCache> tokenCaches;

  public InMemoryTokenCacheService() {
    this(new ArrayList<>());
  }

  public InMemoryTokenCacheService(List<TokenCache> tokenCaches) {
    this.tokenCaches = tokenCaches;
  }

  @Override
  public Observable<TokenCache> create(TokenCache cache) {
    requireNonNull(cache, "Token cache should not be null");
    tokenCaches.add(cache);
    return Observable.just(cache);
  }

  @Override
  public Observable<TokenCache> get(String token, String client) {
    return tokenCaches.stream()
            .filter(tokenCache -> tokenCache.getToken().equals(token) && tokenCache.getClient().equals(client))
            .map(Observable::just)
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException(String.format("TokenCache with such token: %s or client: %s doesn't exist", token, client)));
  }

}

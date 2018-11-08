package ru.ratauth.services;

import ru.ratauth.entities.TokenCache;
import rx.Observable;

/**
 * @author mgorelikov
 * @since 16/02/16
 */
public interface TokenCacheService {
    Observable<TokenCache> create(TokenCache cache);

    Observable<TokenCache> get(String token, String client);
}

package ru.ratauth.server.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.ratauth.entities.*;
import ru.ratauth.server.secutiry.TokenProcessor;
import rx.Observable;

import java.util.Date;

/**
 * @author mgorelikov
 * @since 19/02/16
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class JWTTokenCacheService implements TokenCacheService {
  private final TokenProcessor tokenProcessor;

  @Value("${auth.master_secret}")
  private String masterSecret;//final

  @Override
  public Observable<TokenCache> getToken(Session session, AuthClient authClient, AuthEntry authEntry) {
    final Token token = authEntry.getLatestToken().get();
    return Observable.just(TokenCache.builder()
        .created(new Date())
        .session(session.getId())
        .token(token.getToken())
        .idToken(tokenProcessor.createToken(
            authClient.getSecret(), token.getToken(), token.getCreated(), token.getExpiresIn(),
            authEntry.getScopes(), extractAudience(authEntry.getScopes()),
            authClient.getName(), tokenProcessor.extractUserInfo(session.getUserInfo(), masterSecret)
            )).build());
  }
}

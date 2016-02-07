package ru.ratauth.server.services;

import com.nimbusds.jose.JOSEException;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import ru.ratauth.entities.AuthzEntry;
import ru.ratauth.entities.RelyingParty;
import ru.ratauth.entities.Token;
import ru.ratauth.exception.AuthorizationException;
import ru.ratauth.interaction.*;
import ru.ratauth.server.secutiry.OAuthIssuerImpl;
import ru.ratauth.server.secutiry.OAuthSystemException;
import ru.ratauth.server.secutiry.UUIDValueGenerator;
import ru.ratauth.services.AuthzEntryService;
import ru.ratauth.services.RelyingPartyService;
import rx.Observable;

import java.util.Date;

/**
 * @author mgorelikov
 * @since 03/11/15
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OpenIdAuthTokenService implements AuthTokenService {
  private final RelyingPartyService relyingPartyService;
  private final AuthzEntryService authzEntryService;
  private final TokenProcessor tokenProcessor;
  private final OAuthIssuerImpl codeGenerator = new OAuthIssuerImpl(new UUIDValueGenerator());

  @Value("${auth.token.ttl}")
  private Long tokenTTL;

  //TODO refresh token
  @Override
  @SneakyThrows
  public Observable<TokenResponse> getToken(TokenRequest oauthRequest) throws OAuthSystemException, JOSEException {
    final AuthzEntry authzEntry = loadAuthzEntry(oauthRequest);

    return relyingPartyService.getRelyingParty(authzEntry.getRelyingParty())
        .filter(relyingParty -> authRelyingParty(oauthRequest, authzEntry, relyingParty))
        .flatMap(relyingParty -> createTokenResponse(authzEntry, relyingParty));
  }

  @Override
  public Observable<TokenResponse> createTokenResponse(AuthzEntry authzEntry, RelyingParty relyingParty) {
    return createToken(authzEntry, relyingParty)
        .map(entry -> convertToResponse(authzEntry))
        .switchIfEmpty(Observable.error(new AuthorizationException()));
  }

  @Override
  public Observable<AuthzEntry> createToken(AuthzEntry authzEntry, RelyingParty relyingParty) {
    Date now = new Date();
    Token token = Token.builder()
        .created(now)
        .token(codeGenerator.accessToken())
        .TTL(tokenTTL).build();
    //create jwt token
    String idToken = tokenProcessor.createToken(relyingParty.getSecret(), relyingParty.getBaseAddress(),
        now, token.expiresIn(), token.getToken(),
        authzEntry.getResourceServers(), tokenProcessor.extractUserInfo(authzEntry.getUserInfo(), relyingParty.getSecret()));
    token.setIdToken(idToken);
    authzEntry.addToken(token);
    return authzEntryService.save(authzEntry);
  }

  private TokenResponse convertToResponse(AuthzEntry authzEntry) {
    Token token = authzEntry.getLatestToken();
    return TokenResponse.builder()
        .accessToken(token.getToken())
        .expiresIn(token.expiresIn())
        .tokenType(TokenType.BEARER.toString())
        .idToken(token.getIdToken())
        .refreshToken(authzEntry.getRefreshToken())
        .build();
  }

  private AuthzEntry loadAuthzEntry(TokenRequest oauthRequest) {
    Observable<AuthzEntry> authObs;
    if (oauthRequest.getGrantType() == GrantType.AUTHORIZATION_CODE)
      authObs = authzEntryService.getByValidCode(oauthRequest.getAuthzCode(), new Date());
    else if (oauthRequest.getGrantType() == GrantType.REFRESH_TOKEN)
      authObs = authzEntryService.getByValidRefreshToken(oauthRequest.getRefreshToken(), new Date());
    else throw new AuthorizationException("Invalid grant type");
    return authObs.switchIfEmpty(Observable.error(new AuthorizationException("Authz entry not found")))
        .toBlocking().single();
  }

  private boolean authRelyingParty(TokenRequest oauthRequest, AuthzEntry authCode, RelyingParty relyingParty) {
    return oauthRequest.getClientId().equals(relyingParty.getId())
        && oauthRequest.getClientSecret().equals(relyingParty.getPassword())
        && relyingParty.getResourceServers().containsAll(authCode.getResourceServers());
  }

  @Override
  public Observable<CheckTokenResponse> checkToken(CheckTokenRequest oauthRequest) {
    String accessToken = oauthRequest.getToken();
    return authzEntryService.getByValidToken(accessToken, new Date())
        .filter(authzEntry -> !CollectionUtils.isEmpty(authzEntry.getTokens()))
        .map(entry -> {
              Token token = entry.getTokens().iterator().next();
              return CheckTokenResponse.builder()
                  .idToken(token.getIdToken())
                  .clientId(entry.getRelyingParty())
                  .resourceServers(entry.getResourceServers())
                  .expiresIn(token.expiresIn())
                  .scopes(entry.getScopes())
                  .build();
            }
        ).switchIfEmpty(Observable.error(new AuthorizationException("Token not found")));
  }
}

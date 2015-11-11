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
import ru.ratauth.interaction.*;
import ru.ratauth.server.secutiry.OAuthIssuerImpl;
import ru.ratauth.server.secutiry.OAuthSystemException;
import ru.ratauth.server.secutiry.UUIDValueGenerator;
import ru.ratauth.services.AuthzEntryService;
import ru.ratauth.services.RelyingPartyService;
import rx.Observable;

import java.util.Date;
import java.util.Map;

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
      .flatMap(relyingParty -> {
        Date now = new Date();
        Token token = Token.builder()
          .created(now)
          .token(codeGenerator.accessToken())
          .TTL(tokenTTL).build();
        //create jwt token
        String idToken = tokenProcessor.createToken(relyingParty.getSecret(), relyingParty.getBaseAddress(),
          now, token.expiresIn(), token.getToken(),
          authzEntry.getResourceServers(), tokenProcessor.extractUserInfo(authzEntry.getBaseJWT(), relyingParty.getSecret()));
        token.setIdToken(idToken);
        authzEntry.addToken(token);
        return authzEntryService.save(authzEntry);
      }).map(entry -> {
        Token token = authzEntry.getTokens().get(0);
        return TokenResponse.builder()
          .accessToken(token.getToken())
          .expiresIn(token.expiresIn())
          .tokenType(TokenType.BEARER.toString())
          .idToken(token.getIdToken())
          .refreshToken(authzEntry.getRefreshToken())
          .build();
      }).switchIfEmpty(Observable.error(new AuthorizationException()));
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
          Token token = entry.getTokens().get(0);
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

  private static class TokenData {
    final AuthzEntry authCode;
    final Map<String, String> userInfo;

    public TokenData(AuthzEntry authCode, Map<String, String> userInfo) {
      this.authCode = authCode;
      this.userInfo = userInfo;
    }
  }

  @SneakyThrows
  private String generateToken() {
    return codeGenerator.accessToken();
  }
}

package ru.ratauth.server.services;

import com.nimbusds.jose.JOSEException;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.ratauth.entities.AuthCode;
import ru.ratauth.entities.AuthCodeStatus;
import ru.ratauth.entities.RelyingParty;
import ru.ratauth.entities.Token;
import ru.ratauth.interaction.*;
import ru.ratauth.providers.AuthProvider;
import ru.ratauth.server.secutiry.OAuthIssuerImpl;
import ru.ratauth.server.secutiry.OAuthSystemException;
import ru.ratauth.server.secutiry.UUIDValueGenerator;
import ru.ratauth.services.AuthCodeService;
import ru.ratauth.services.RelyingPartyService;
import ru.ratauth.services.TokenService;
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
  private final TokenService tokenService;
  private final AuthCodeService authCodeService;
  private final Map<String, AuthProvider> authProviders;
  private final TokenGenerator tokenGenerator;
  private final OAuthIssuerImpl codeGenerator = new OAuthIssuerImpl(new UUIDValueGenerator());

  @Value("${auth.token.ttl}")
  private Long tokenTTL;

  //TODO refresh token
  @Override
  @SneakyThrows
  public Observable<TokenResponse> getToken(TokenRequest oauthRequest) throws OAuthSystemException, JOSEException {
    final Observable<AuthCode> authCodeObs = authCodeService.get(oauthRequest.getCode()).cache();

    return authCodeObs.flatMap(authCode -> {
      if (authCode.getStatus() == AuthCodeStatus.NEW)
        return relyingPartyService.getRelyingParty(authCode.getRelyingParty());
      throw new AuthorizationException("Auth code not found");
    }).flatMap(relyingParty -> {
      AuthCode authCode = authCodeObs.toBlocking().single();
      if (!oauthRequest.getClientId().equals(relyingParty.getId())
          || !oauthRequest.getClientSecret().equals(relyingParty.getPassword())
          || !relyingParty.getResourceServers().containsAll(authCode.getResourceServers()))
        throw new AuthorizationException("Auth code does not belong to relying party");

      //persist auth code
      authCode.setStatus(AuthCodeStatus.USED);
      authCode.setUsed(new Date());
      Observable<AuthCode> authCodeSaveObs = authCodeService.save(authCode);

      Observable<Map<String, String>> userInfoObs = authProviders.get(relyingParty.getIdentityProvider())
          .checkCredentials(oauthRequest.getUsername(), oauthRequest.getPassword());
      return Observable.zip(userInfoObs, authCodeSaveObs,
          (userInfo, code) -> {
            Token token = createToken(generateToken(), authCode, userInfo.get(AuthProvider.USER_ID));
            token.setTokenId(tokenGenerator.createToken(relyingParty, token, userInfo));
            return token;
          });
    }).flatMap(token -> tokenService.save(token))
        .map(token -> TokenResponse.builder()
            .accessToken(token.getToken())
            .expiresIn(token.expiresIn())
            .tokenType(TokenType.BEARER.toString())
            .idToken(token.getTokenId())
            .build())
        .switchIfEmpty(Observable.error(new AuthorizationException()));
  }

  @Override
  public Observable<CheckTokenResponse> checkToken(CheckTokenRequest oauthRequest) {
    String accessToken = oauthRequest.getToken();
    return tokenService.get(accessToken).map(token -> {
          if (token != null && token.expiresIn() > System.currentTimeMillis()) {
            return CheckTokenResponse.builder()
                .tokenId(token.getTokenId())
                .clientId(token.getRelyingParty())
                .resourceServers(token.getResourceServers())
                .expiresIn(token.expiresIn())
                .userId(token.getUser())
                .scopes(token.getScopes())
                .build();
          } else {
            throw new CheckTokenException("Token has been expired");
          }
        }
    );

  }

  private static class TokenData {
    final AuthCode authCode;
    final Map<String, String> userInfo;

    public TokenData(AuthCode authCode, Map<String, String> userInfo) {
      this.authCode = authCode;
      this.userInfo = userInfo;
    }
  }

  @SneakyThrows
  private String generateToken() {
    return codeGenerator.accessToken();
  }

  private Token createToken(String token, AuthCode authCode, String userId) {
    return Token.builder()
        .codeId(authCode.getId())
        .token(token)
        .created(new Date())
        .TTL(tokenTTL)
        .relyingParty(authCode.getRelyingParty())
        .identityProvider(authCode.getIdentityProvider())
        .scopes(authCode.getScopes())
        .user(userId)
        .build();
  }
}

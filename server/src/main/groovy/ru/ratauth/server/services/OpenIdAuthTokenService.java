package ru.ratauth.server.services;

import com.nimbusds.jose.JOSEException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.ratauth.entities.AuthCode;
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
  public TokenResponse getToken(TokenRequest oauthRequest) throws OAuthSystemException, JOSEException {
    TokenResponse response = null;
    AuthCode authCode = authCodeService.get(oauthRequest.getCode());
    if(authCode != null ){
      RelyingParty relyingParty = relyingPartyService.getRelyingParty(authCode.getRelyingParty());
      if(oauthRequest.getClientId().equals(relyingParty.getId())
          && oauthRequest.getClientSecret().equals(relyingParty.getPassword())
          && relyingParty.getResourceServers().containsAll(authCode.getResourceServers())) {
        Map<String, String> userInfo = authProviders.get(relyingParty.getIdentityProvider())
            .checkCredentials(oauthRequest.getUsername(), oauthRequest.getPassword());
        if(userInfo != null) {
          Token token = createToken(codeGenerator.accessToken(), authCode, userInfo.get(AuthProvider.USER_ID));
          token.setTokenId(tokenGenerator.createToken(relyingParty, token, userInfo));
          tokenService.save(token);
          return TokenResponse.builder()
              .accessToken(token.getToken())
              .expiresIn(token.expiresIn())
              .tokenType(TokenType.BEARER.toString())
              .idToken(token.getTokenId())
              .build();
        }
      }
    }
    return response;
  }

  @Override
  public CheckTokenResponse checkToken(CheckTokenRequest oauthRequest) {
    CheckTokenResponse checkTokenResponse = null;
    String accessToken = oauthRequest.getToken();
    Token token = tokenService.get(accessToken);
    if(token != null && token.expiresIn() > System.currentTimeMillis()) {
      checkTokenResponse = CheckTokenResponse.builder()
          .tokenId(token.getTokenId())
          .clientId(token.getRelyingParty())
          .resourceServers(token.getResourceServers())
          .expiresIn(token.expiresIn())
          .userId(token.getUser())
          .scopes(token.getScopes())
          .build();
    }
    return checkTokenResponse;
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

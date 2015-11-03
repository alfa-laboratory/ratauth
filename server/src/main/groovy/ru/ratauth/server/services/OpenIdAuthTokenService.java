package ru.ratauth.server.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.apache.oltu.oauth2.as.issuer.OAuthIssuerImpl;
import org.apache.oltu.oauth2.as.issuer.UUIDValueGenerator;
import org.apache.oltu.oauth2.as.request.OAuthTokenRequest;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.types.ParameterStyle;
import org.apache.oltu.oauth2.common.message.types.TokenType;
import org.apache.oltu.oauth2.rs.request.OAuthAccessResourceRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.ratauth.entities.AuthCode;
import ru.ratauth.entities.RelyingParty;
import ru.ratauth.entities.Token;
import ru.ratauth.interaction.CheckTokenResponse;
import ru.ratauth.interaction.TokenResponse;
import ru.ratauth.providers.AuthProvider;
import ru.ratauth.services.AuthCodeService;
import ru.ratauth.services.RelyingPartyService;
import ru.ratauth.services.TokenService;

import javax.servlet.http.HttpServletRequest;
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

  @Value("${auth.signature}")
  private String signature;

  //TODO refresh token
  @Override
  public TokenResponse getToken(HttpServletRequest request) throws OAuthProblemException, OAuthSystemException, JsonProcessingException {
    TokenResponse response = null;
    OAuthTokenRequest oauthRequest = new OAuthTokenRequest(request);
    AuthCode authCode = authCodeService.get(oauthRequest.getParam(OAuth.OAUTH_CODE));
    if(authCode != null ){
      RelyingParty relyingParty = relyingPartyService.getRelyingParty(authCode.getRelyingParty());
      if(oauthRequest.getClientId().equals(relyingParty.getId()) && oauthRequest.getClientSecret().equals(relyingParty.getSecret())) {
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
  public CheckTokenResponse checkToken(HttpServletRequest request) throws OAuthProblemException, OAuthSystemException {
    CheckTokenResponse checkTokenResponse = null;
    OAuthAccessResourceRequest oauthRequest = new OAuthAccessResourceRequest(request,
        ParameterStyle.BODY);
    String accessToken = oauthRequest.getAccessToken();
    Token token = tokenService.get(accessToken);
    if(token != null && token.expiresIn() > System.currentTimeMillis()) {
      RelyingParty relyingParty = relyingPartyService.getRelyingParty(token.getRelyingParty());
      checkTokenResponse = CheckTokenResponse.builder()
          .tokenId(token.getTokenId())
          .clientId(token.getRelyingParty())
          .resourceServers(relyingParty.getResourceServers())
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

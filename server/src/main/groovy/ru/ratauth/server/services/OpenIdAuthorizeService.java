package ru.ratauth.server.services;

import lombok.RequiredArgsConstructor;
import org.apache.oltu.oauth2.as.issuer.MD5Generator;
import org.apache.oltu.oauth2.as.issuer.OAuthIssuerImpl;
import org.apache.oltu.oauth2.as.request.OAuthAuthzRequest;
import org.apache.oltu.oauth2.as.response.OAuthASResponse;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.OAuthResponse;
import org.apache.oltu.oauth2.common.message.types.ResponseType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import ru.ratauth.entities.AuthCode;
import ru.ratauth.entities.AuthCodeStatus;
import ru.ratauth.entities.RelyingParty;
import ru.ratauth.entities.Token;
import ru.ratauth.providers.AuthProvider;
import ru.ratauth.server.utils.StringUtils;
import ru.ratauth.services.AuthCodeService;
import ru.ratauth.services.RelyingPartyService;
import ru.ratauth.services.TokenService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.Map;
import java.util.Set;

/**
 * @author mgorelikov
 * @since 02/11/15
 */
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OpenIdAuthorizeService implements AuthorizeService {
  private final RelyingPartyService relyingPartyService;
  private final TokenService tokenService;
  private final AuthCodeService authCodeService;
  private final Map<String, AuthProvider> authProviders;
  private final OAuthIssuerImpl codeGenerator = new OAuthIssuerImpl(new MD5Generator());

  @Value("${auth.code.ttl}")
  private Long codeTTL;

  @Override
  public String authenticate(HttpServletRequest request) throws URISyntaxException, OAuthSystemException, OAuthProblemException {
    OAuthAuthzRequest oauthRequest = new OAuthAuthzRequest(request);

    RelyingParty relyingParty = relyingPartyService.getRelyingParty(oauthRequest.getClientId());
    OAuthASResponse.OAuthAuthorizationResponseBuilder builder = OAuthASResponse
        .authorizationResponse(request, HttpServletResponse.SC_FOUND);

    //only for response type TOKEN//TODO think about type code
    AuthCode authCode = saveCode(codeGenerator.authorizationCode(), relyingParty, oauthRequest.getScopes());

    String redirectURI = oauthRequest.getParam(OAuth.OAUTH_REDIRECT_URI);
    if(StringUtils.isBlank(redirectURI)) {
      redirectURI = relyingParty.getRedirectURL();
    }

    return builder.setCode(authCode.getCode())
        .setExpiresIn(authCode.expiresIn())
        .location(redirectURI)
        .buildQueryMessage()
        .getLocationUri();
  }



  private AuthCode saveCode(String code, RelyingParty relyingParty, Set<String> scopes) {
    return authCodeService.save(
        AuthCode.builder()
            .code(code)
            .created(new Date())
            .TTL(codeTTL)
            .relyingParty(relyingParty.getId())
            .identityProvider(relyingParty.getIdentityProvider())
            .scopes(scopes)
            .status(AuthCodeStatus.NEW)
            .build());
  }
}

package ru.ratauth.server.services;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.ratauth.entities.AuthzEntry;
import ru.ratauth.entities.RelyingParty;
import ru.ratauth.entities.Token;
import ru.ratauth.exception.AuthorizationException;
import ru.ratauth.interaction.AuthzRequest;
import ru.ratauth.interaction.AuthzResponse;
import ru.ratauth.interaction.AuthzResponseType;
import ru.ratauth.interaction.TokenType;
import ru.ratauth.providers.auth.AuthProvider;
import ru.ratauth.providers.auth.dto.AuthInput;
import ru.ratauth.server.secutiry.OAuthIssuerImpl;
import ru.ratauth.server.secutiry.UUIDValueGenerator;
import ru.ratauth.utils.StringUtils;
import ru.ratauth.services.AuthzEntryService;
import ru.ratauth.services.RelyingPartyService;
import rx.Observable;

import java.util.Date;
import java.util.Map;

/**
 * @author mgorelikov
 * @since 02/11/15
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OpenIdAuthorizeService implements AuthorizeService {
  private final RelyingPartyService relyingPartyService;
  private final AuthzEntryService authzEntryService;
  private final Map<String, AuthProvider> authProviders;
  private final TokenProcessor tokenProcessor;
  private final AuthTokenService authTokenService;
  private final OAuthIssuerImpl codeGenerator = new OAuthIssuerImpl(new UUIDValueGenerator());

  @Value("${auth.code.ttl}")
  private Long codeTTL;//final
  @Value("${auth.refresh_token.ttl}")
  private Long refreshTokenTTL;//final

  @Override
  @SneakyThrows
  public Observable<AuthzResponse> authenticate(AuthzRequest oauthRequest) {

    //load corresponding relying party
    final RelyingParty relyingParty = relyingPartyService.getRelyingParty(oauthRequest.getClientId())
      .filter(rp -> authRelyingParty(oauthRequest,rp))
      .filter(rp -> oauthRequest.getAuds() == null || rp.getResourceServers().containsAll(oauthRequest.getAuds()))//check rights
      .switchIfEmpty(Observable.error(new AuthorizationException("RelyingParty not found")))
      .toBlocking().single();

    //authorize
    return authProviders.get(relyingParty.getIdentityProvider())
      .authenticate(AuthInput.builder().data(oauthRequest.getAuthData()).relyingParty(relyingParty.getName()).build())
      .flatMap(userInfo -> {
        Date now = new Date();
        //create entry
        AuthzEntry authzEntry = AuthzEntry.builder()
            .authCode(codeGenerator.authorizationCode())
            .created(new Date())
            .codeTTL(codeTTL)
            .refreshToken(codeGenerator.refreshToken())
            .refreshTokenTTL(refreshTokenTTL)
            .relyingParty(relyingParty.getId())
            .identityProvider(relyingParty.getIdentityProvider())
            .scopes(oauthRequest.getScopes())
            .resourceServers(oauthRequest.getAuds() == null ? relyingParty.getResourceServers() : oauthRequest.getAuds())
            .build();
        //create base JWT
        String userJWT = tokenProcessor.createToken(relyingParty.getSecret(), relyingParty.getBaseAddress(),
            now, authzEntry.codeExpiresIn(), authzEntry.getAuthCode(),
            authzEntry.getResourceServers(), userInfo.getData());
        authzEntry.setUserInfo(userJWT);
        return authzEntryService.save(authzEntry);
      }).flatMap(entry -> {
          if (oauthRequest.getResponseType() == AuthzResponseType.TOKEN)
            return authTokenService.createToken(entry, relyingParty);
          else
            return Observable.just(entry);
        }).map(entry -> buildResponse(oauthRequest,entry,relyingParty)
        ).switchIfEmpty(Observable.error(new AuthorizationException("Authorization error")));
  }

  private static AuthzResponse buildResponse(AuthzRequest oauthRequest, AuthzEntry entry, RelyingParty relyingParty) {
    String redirectURI = StringUtils.isBlank(oauthRequest.getRedirectURI())? oauthRequest.getRedirectURI() : relyingParty.getRedirectURL();
    AuthzResponse resp = AuthzResponse.builder()
        .location(redirectURI).build();
    Token token = entry.getLatestToken();
    if(token != null) {
      resp.setToken(token.getToken());
      resp.setIdToken(token.getIdToken());
      resp.setTokenType(TokenType.BEARER);
      resp.setRefreshToken(entry.getRefreshToken());
      resp.setExpiresIn(token.expiresIn());
    } else {
      resp.setCode(entry.getAuthCode());
      resp.setExpiresIn(entry.codeExpiresIn());
    }
    return resp;
  }

  private boolean authRelyingParty(AuthzRequest oauthRequest, RelyingParty relyingParty) {
    return oauthRequest.getResponseType() == AuthzResponseType.CODE
        || oauthRequest.getClientId().equals(relyingParty.getId())
        && oauthRequest.getClientSecret().equals(relyingParty.getPassword());
  }
}

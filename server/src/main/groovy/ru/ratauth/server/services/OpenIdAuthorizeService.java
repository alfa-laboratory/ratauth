package ru.ratauth.server.services;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.ratauth.entities.AuthzEntry;
import ru.ratauth.entities.RelyingParty;
import ru.ratauth.interaction.AuthzRequest;
import ru.ratauth.interaction.AuthzResponse;
import ru.ratauth.providers.AuthProvider;
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
  private final OAuthIssuerImpl codeGenerator = new OAuthIssuerImpl(new UUIDValueGenerator());

  @Value("${auth.code.ttl}")
  private Long codeTTL;//final
  @Value("${auth.refresh_token.ttl}")
  private Long refreshTokenTTL;//final

  @Override
  @SneakyThrows
  public Observable<AuthzResponse> authenticate(AuthzRequest oauthRequest) {

    final RelyingParty relyingParty = relyingPartyService.getRelyingParty(oauthRequest.getClientId())
      .filter(rp -> oauthRequest.getAuds() == null || rp.getResourceServers().containsAll(oauthRequest.getAuds()))//if aud is empty we will get relyingParty resourceServers
      .switchIfEmpty(Observable.error(new AuthorizationException("RelyingParty not found")))
      .toBlocking().single();


    return authProviders.get(relyingParty.getIdentityProvider())
      .checkCredentials(oauthRequest.getUsername(), oauthRequest.getPassword())
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
          authzEntry.getResourceServers(), userInfo);
        authzEntry.setUserInfo(userJWT);
        return authzEntryService.save(authzEntry);
      }).map(entry -> {
        String redirectURI = oauthRequest.getRedirectURI();
        if (StringUtils.isBlank(redirectURI)) {
          redirectURI = relyingParty.getRedirectURL();
        }
        return AuthzResponse.builder()
          .code(entry.getAuthCode())
          .expiresIn(entry.codeExpiresIn())
          .location(redirectURI).build();
      }).switchIfEmpty(Observable.error(new AuthorizationException("Authorization error")));
  }
}

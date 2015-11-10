package ru.ratauth.server.services;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.ratauth.entities.AuthCode;
import ru.ratauth.entities.AuthCodeStatus;
import ru.ratauth.entities.RelyingParty;
import ru.ratauth.interaction.AuthzRequest;
import ru.ratauth.interaction.AuthzResponse;
import ru.ratauth.server.secutiry.MD5Generator;
import ru.ratauth.server.secutiry.OAuthIssuerImpl;
import ru.ratauth.server.secutiry.OAuthSystemException;
import ru.ratauth.utils.StringUtils;
import ru.ratauth.services.AuthCodeService;
import ru.ratauth.services.RelyingPartyService;
import ru.ratauth.services.TokenService;
import rx.Observable;

import java.net.URISyntaxException;
import java.util.Date;
import java.util.Set;

/**
 * @author mgorelikov
 * @since 02/11/15
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OpenIdAuthorizeService implements AuthorizeService {
  private final RelyingPartyService relyingPartyService;
  private final TokenService tokenService;
  private final AuthCodeService authCodeService;
  private final OAuthIssuerImpl codeGenerator = new OAuthIssuerImpl(new MD5Generator());

  @Value("${auth.code.ttl}")
  private Long codeTTL;

  //TODO think about response type code
  @Override
  @SneakyThrows
  public Observable<AuthzResponse> authenticate(AuthzRequest oauthRequest) {

    Observable<RelyingParty> relyingPartyObs = relyingPartyService.getRelyingParty(oauthRequest.getClientId()).cache();

    return relyingPartyObs.flatMap(relyingParty ->
        saveCode(generateCode(), relyingParty, oauthRequest.getScopes())//only for response type TOKEN
    ).map(authCode -> {
      String redirectURI = oauthRequest.getRedirectURI();
      if (StringUtils.isBlank(redirectURI)) {
        redirectURI = relyingPartyObs.toBlocking().single().getRedirectURL();
      }

      return AuthzResponse.builder()
          .code(authCode.getCode())
          .expiresIn(authCode.expiresIn())
          .location(redirectURI).build();
    }).switchIfEmpty(Observable.error(new OAuthSystemException("Relying party not found")));
  }

  @SneakyThrows
  private String generateCode() {
    return codeGenerator.authorizationCode();
  }

  private Observable<AuthCode> saveCode(String code, RelyingParty relyingParty, Set<String> scopes) {
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

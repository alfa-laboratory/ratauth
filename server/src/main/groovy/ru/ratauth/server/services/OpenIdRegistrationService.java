package ru.ratauth.server.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.ratauth.entities.RelyingParty;
import ru.ratauth.exception.AuthorizationException;
import ru.ratauth.interaction.AuthzResponse;
import ru.ratauth.interaction.GrantType;
import ru.ratauth.interaction.RegistrationRequest;
import ru.ratauth.interaction.TokenResponse;
import ru.ratauth.providers.registrations.RegistrationProvider;
import ru.ratauth.providers.registrations.dto.RegInput;
import ru.ratauth.providers.registrations.dto.RegResult;
import ru.ratauth.services.RelyingPartyService;
import rx.Observable;
import ru.ratauth.providers.registrations.dto.RegResult.Status;

import java.util.Map;

/**
 * @author mgorelikov
 * @since 29/01/16
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OpenIdRegistrationService implements RegistrationService {
  private final RelyingPartyService relyingPartyService;
  private final Map<String, RegistrationProvider> registerProviders;
  private final AuthorizeService authorizeService;
  private final AuthTokenService tokenService;

  @Override
  public Observable<RegResult> register(RegistrationRequest request) {
    final RelyingParty relyingParty = loadRelyingParty(request);

    return registerProviders.get(relyingParty.getIdentityProvider())
        .register(RegInput.builder().relyingParty(relyingParty.getName()).data(request.getData()).build());
  }

  @Override
  public Observable<TokenResponse> finishRegister(RegistrationRequest request) {
    final RelyingParty relyingParty = loadRelyingParty(request);
    return registerProviders.get(relyingParty.getIdentityProvider())
        .register(RegInput.builder().relyingParty(relyingParty.getName()).data(request.getData()).build())
        .flatMap(regResult -> authorizeService.createEntry(relyingParty,request.getAuds(), null, regResult.getData()))
        .flatMap(entry -> tokenService.createTokenResponse(entry, relyingParty));
  }

  public RelyingParty loadRelyingParty(RegistrationRequest request) {
    return relyingPartyService.getRelyingParty(request.getClientId())
        .filter(rp -> authRelyingParty(request,rp))
        .filter(rp -> request.getAuds() == null || rp.getResourceServers().containsAll(request.getAuds()))//check rights
        .switchIfEmpty(Observable.error(new AuthorizationException("RelyingParty not found")))
        .toBlocking().single();
  }

  public boolean authRelyingParty(RegistrationRequest request, RelyingParty relyingParty) {
    return GrantType.AUTHORIZATION_CODE != request.getGrantType()
        || request.getClientId().equals(relyingParty.getId())
        && request.getClientSecret().equals(relyingParty.getPassword());
  }
}

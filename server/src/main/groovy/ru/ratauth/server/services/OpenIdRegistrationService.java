package ru.ratauth.server.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.ratauth.entities.Assurance;
import ru.ratauth.entities.RelyingParty;
import ru.ratauth.entities.Session;
import ru.ratauth.exception.RegistrationException;
import ru.ratauth.interaction.AuthzResponseType;
import ru.ratauth.interaction.RegistrationRequest;
import ru.ratauth.interaction.RegistrationResponse;
import ru.ratauth.interaction.TokenResponse;
import ru.ratauth.providers.registrations.RegistrationProvider;
import ru.ratauth.providers.registrations.dto.AssuredRegResult;
import ru.ratauth.providers.registrations.dto.RegInput;
import ru.ratauth.providers.registrations.dto.RegResult;
import ru.ratauth.utils.StringUtils;
import ru.ratauth.utils.URIUtils;
import rx.Observable;

import java.util.Map;

/**
 * @author mgorelikov
 * @since 29/01/16
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OpenIdRegistrationService implements RegistrationService {
  private final Map<String, RegistrationProvider<RegResult>> registrationProviders;
  private final AuthSessionService sessionService;
  private final AuthClientService authClientService;
  private final AuthAssuranceService assuranceService;

  @Override
  public Observable<RegistrationResponse> register(RegistrationRequest request) {
    return authClientService.loadRelyingParty(request.getClientId())
        .flatMap(rp -> registrationProviders.get(rp.getIdentityProvider())
            .register(RegInput.builder().relyingParty(rp.getName()).data(request.getData()).build())
            .flatMap(regResult -> createSession(regResult, rp, request)
                .flatMap(regSession -> enrollAssurance(regSession,regResult, rp, request), ImmutablePair::new))
            .map(sessionACR -> convertToResponse(rp, request, sessionACR.getLeft(), sessionACR.getRight()))
        )
        .doOnCompleted(() -> log.info("First step of registration succeed"));
  }

  private Observable<Session> createSession(RegResult regResult, RelyingParty relyingParty, RegistrationRequest request) {
    return sessionService.initSession(relyingParty, regResult.getUserId(), regResult.getUserInfo(),
        request.getScopes(), request.getRedirectURI(), regResult.getAssuranceLevel());
  }

  private Observable<Assurance> enrollAssurance(Session session, RegResult regResult, RelyingParty relyingParty, RegistrationRequest request) {
    if(regResult.getStatus() == RegResult.Status.NEED_APPROVAL) {
      AssuredRegResult acrResult =  (AssuredRegResult)regResult;
      return assuranceService.create(session.getId(), relyingParty.getName(), relyingParty.getIdentityProvider(), acrResult.getAssuranceStatus(),
          request.getAcrValues(), regResult.getAssuranceLevel(), acrResult.getUserInfo(), acrResult.getAssuranceData());
    } else {
      return Observable.just(null);
    }
  }

  private RegistrationResponse convertToResponse(RelyingParty relyingParty, RegistrationRequest request, Session session, Assurance assurance) {
    String redirectURI = createRedirectURL(relyingParty, request.getRedirectURI());

    return RegistrationResponse.builder()
        .redirectUrl(redirectURI)
        .code(session.getEntry(relyingParty.getName()).get().getAuthCode())
        .enrollmentId(assurance.getId())
        .build();
  }

  private static String createRedirectURL(RelyingParty relyingParty, String redirectURL) {
    String redirectURI = redirectURL;
    if (StringUtils.isBlank(redirectURI)) {
      redirectURI = relyingParty.getRegistrationRedirectURI();
    } else {
      if (!URIUtils.compareHosts(redirectURI, relyingParty.getRedirectURIs()))
        throw new RegistrationException(RegistrationException.ID.REDIRECT_NOT_CORRECT);
    }
    return redirectURI;
  }
}

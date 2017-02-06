package ru.ratauth.server.services;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Service;
import ru.ratauth.entities.assurance.Assurance;
import ru.ratauth.entities.RelyingParty;
import ru.ratauth.entities.Session;
import ru.ratauth.entities.assurance.FactorProviderData;
import ru.ratauth.interaction.EnrollmentRequest;
import ru.ratauth.providers.assurance.FactorProvider;
import ru.ratauth.providers.assurance.dto.AssuranceStatus;
import ru.ratauth.providers.assurance.dto.EnrollmentInput;
import ru.ratauth.providers.assurance.dto.EnrollmentResult;
import ru.ratauth.services.AssuranceService;
import ru.ratauth.services.SessionService;
import rx.Observable;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author mgorelikov
 * @since 31/01/17
 */
@Service
@RequiredArgsConstructor
public class OpenIdAuthAssuranceService implements AuthAssuranceService {
  private final AssuranceService assuranceService;
  private final SessionService sessionService;
  private final AuthClientService authClientService;

  private final Map<String, FactorProvider> factorProviders;

  @Override
  public Observable<Assurance> enroll(EnrollmentRequest request) {
    Observable<RelyingParty> rpObs =
        authClientService.loadAndAuthRelyingParty(request.getClientId(), request.getClientPassword(), true);
    Observable<Session>  sessionObs =
        sessionService.getByValidToken(request.getAccessToken(), new Date());
    return Observable.zip(sessionObs, rpObs, ImmutablePair::new)
        .flatMap(sessionRp -> enroll(sessionRp.getLeft(), sessionRp.getRight(), request.getAcrValues())
    );
  }

  @Override
  public Observable<Assurance> enroll(Session session, RelyingParty relyingParty, Set<String> acrValues) {
    return null;
  }

  @Override
  public Observable<Assurance> activate(String assuranceId) {
    return null;
  }

  @Override
  public Observable<Assurance> verify(String assuranceId, Map<String, Object> verificationData) {
    return null;
  }

  @Override
  public Observable<Assurance> create(String sessionId, String relyingPartyName, String identityProvider,
                                      AssuranceStatus status, Set<String> acrValues, String acr,
                                      Map<String, Object> userInfo, Map<String, FactorProviderData> providerData) {
    Date now = new Date();

    return assuranceService.create(
        Assurance.builder()
            .sessionId(sessionId)
            .status(status)
            .acrValues(acrValues)
            .acr(acr)
            .activatedFactorProvider(identityProvider)
            .relyingParty(relyingPartyName)
            .additionalUserInfo(userInfo)
            .providerData(providerData)
            .created(now)
            .lastUpdate(now).build()
    );
  }

  private Observable<Map<String, FactorProviderData>> requestProviders(Set<String> providerNames, String userId) {
    List<Observable<EnrollmentResult>> providersInfo = providerNames.stream().map(name ->
        factorProviders.get(name).enroll(EnrollmentInput.builder().userId(userId).build()))
        .collect(Collectors.toList());
    return null;//todo
  }

  private Set<String> filterProviders(Set<String> rpFactorProviders ,Set<String> acrValues) {
    return rpFactorProviders.stream().filter(name -> factorProviders.containsKey(name) &&
        acrValues.contains(factorProviders.get(name).getProvidedAssuranceLevel())).collect(Collectors.toSet());
  }
}

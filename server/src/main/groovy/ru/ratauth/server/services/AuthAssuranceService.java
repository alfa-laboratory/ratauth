package ru.ratauth.server.services;

import ru.ratauth.entities.assurance.Assurance;
import ru.ratauth.entities.RelyingParty;
import ru.ratauth.entities.Session;
import ru.ratauth.entities.assurance.FactorProviderData;
import ru.ratauth.interaction.EnrollmentRequest;
import ru.ratauth.providers.assurance.dto.AssuranceStatus;
import rx.Observable;

import java.util.Map;
import java.util.Set;

/**
 * @author mgorelikov
 * @since 31/01/17
 */
public interface AuthAssuranceService {

  Observable<Assurance> enroll(EnrollmentRequest request);

  Observable<Assurance> enroll(Session session, RelyingParty relyingParty, Set<String> acrValues);

  Observable<Assurance> activate(String assuranceId);

  Observable<Assurance> verify(String assuranceId, Map<String,Object> verificationData);

  Observable<Assurance> create(String sessionId, String relyingPartyName, String identityProvider,
                               AssuranceStatus status, Set<String> acrValues, String acr,
                               Map<String, Object> userInfo, Map<String, FactorProviderData> providerData);
}

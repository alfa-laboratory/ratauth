package ru.ratauth.server.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.ratauth.entities.Assurance;
import ru.ratauth.entities.RelyingParty;
import ru.ratauth.entities.Session;
import ru.ratauth.providers.assurance.dto.AssuranceStatus;
import ru.ratauth.server.configuration.SignatureConfig;
import ru.ratauth.server.secutiry.TokenProcessor;
import ru.ratauth.server.utils.DateUtils;
import ru.ratauth.services.AssuranceService;
import rx.Observable;

import java.util.Date;
import java.util.Map;
import java.util.Set;

/**
 * @author mgorelikov
 * @since 31/01/17
 */
@Service
@RequiredArgsConstructor
public class OpenIdAuthAssuranceService implements AuthAssuranceService {
  private final AssuranceService assuranceService;
  private final TokenProcessor tokenProcessor;
  private final SignatureConfig signatureConfig;

  public static final String RATAUTH = "ratauth";

  @Override
  public Observable<Assurance> enroll(Session session, RelyingParty relyingParty) {
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
                                      Map<String, Object> userInfo, Map<String, Object> enrollmentInfo) {
    Date now = new Date();

    return assuranceService.create(
        Assurance.builder()
            .sessionId(sessionId)
            .status(status)
            .acrValues(acrValues)
            .acr(acr)
            .identityProvider(identityProvider)
            .relyingParty(relyingPartyName)
            .additionalUserInfo(userInfo)
            .enrollmentInfo(enrollmentInfo)
            .created(now)
            .lastUpdate(now).build()
    );
  }
}

package ru.ratauth.server.extended.restriction;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ratauth.entities.AcrValues;
import ru.ratauth.entities.Session;
import ru.ratauth.interaction.AuthzRequest;
import ru.ratauth.providers.auth.dto.VerifyResult;
import ru.ratauth.server.configuration.IdentityProvidersConfiguration;
import ru.ratauth.server.configuration.RestrictionConfiguration;
import ru.ratauth.server.extended.enroll.activate.ActivateEnrollRequest;
import ru.ratauth.services.RestrictionService;

import java.util.List;

import static ru.ratauth.providers.Fields.USER_ID;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CheckRestrictionService {

    private final IdentityProvidersConfiguration identityProvidersConfiguration;
    private final RestrictionService restrictionService;

    public void checkAuthRestrictions(AuthzRequest request, VerifyResult verifyResult) {
        RestrictionConfiguration restrictionConfiguration = identityProvidersConfiguration.getIdp().get(request.getEnroll()).getRestrictions();
        String clientId = request.getClientId();
        if (shouldIncrementRestrictionCount(restrictionConfiguration, clientId)) {
            restrictionService.checkIsAuthAllowed(clientId,
                    verifyResult.getData().get(USER_ID.val()).toString(),
                    verifyResult.getAcrValues(),
                    restrictionConfiguration.getAttemptMaxValue(),
                    restrictionConfiguration.getTtlInSeconds());
        }
    }

    public void checkAuthRestrictions(Session session, ActivateEnrollRequest request) {
        RestrictionConfiguration restrictionConfiguration = identityProvidersConfiguration.getIdp().get(request.getEnroll().getFirst()).getRestrictions();
        String clientId = request.getClientId();
        if (shouldIncrementRestrictionCount(restrictionConfiguration, clientId)) {
            restrictionService.checkIsAuthAllowed(clientId,
                    session.getUserId(),
                    (AcrValues) request.getEnroll(),
                    restrictionConfiguration.getAttemptMaxValue(),
                    restrictionConfiguration.getTtlInSeconds());
        }

    }

    private List<String> getClientIdRestriction(RestrictionConfiguration restrictionConfiguration) {
        return restrictionConfiguration == null ? null : restrictionConfiguration.getClientId();
    }

    private boolean shouldIncrementRestrictionCount(RestrictionConfiguration restrictionConfiguration, String requestClientId) {
        List<String> clientIdsRestriction = getClientIdRestriction(restrictionConfiguration);
        return clientIdsRestriction != null && clientIdsRestriction.contains(requestClientId);
    }
}

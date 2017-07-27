package ru.ratauth.server.providers;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.alfabank.api.auth.methods.AuthMethod;
import ru.ratauth.entities.AcrValues;
import ru.ratauth.entities.IdentityProvider;
import ru.ratauth.providers.auth.dto.ActivateInput;
import ru.ratauth.providers.auth.dto.ActivateResult;
import ru.ratauth.providers.auth.dto.VerifyInput;
import ru.ratauth.providers.auth.dto.VerifyResult;
import ru.ratauth.server.services.ReferenceService;
import rx.Observable;

import static ru.ratauth.providers.Fields.USER_ID;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ClickIdentityProvider implements IdentityProvider {

    private final DCAIdentityProvider dcaIdentityProvider;
    private final ReferenceService referenceService;

    @Value("CLICK")
    private String name;

    @Override
    public String name() {
        return name;
    }

    @Override
    public Observable<ActivateResult> activate(ActivateInput input) {
        return dcaIdentityProvider.activate(input);
    }

    @Override
    public Observable<VerifyResult> verify(VerifyInput input) {
        return dcaIdentityProvider.verify(input)
                .map(response -> {
                    String userId = (String) response.getData().get(USER_ID.val());
                    String relyingParty = input.getRelyingParty();
                    if (isSmsPwdRequired(userId, relyingParty)) {
                        response.setAcrValues(AcrValues.valueOf("sms"));
                        return response;
                    }
                    return response;
                });
    }

    private boolean isSmsPwdRequired(String userId, String relyingParty) {
        return referenceService.create(userId, relyingParty)
                .map(response -> response.getAuthMethods().stream()
                        .map(AuthMethod::getCode)
                        .filter(StringUtils::isNotBlank)
                        .filter(value -> value.equals("SMSPWD"))
                        .count() > 0).toBlocking().single();
    }
}

package ru.ratauth.server.providers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.ratauth.entities.AcrValues;
import ru.ratauth.entities.Enroll;
import ru.ratauth.entities.IdentityProvider;
import ru.ratauth.providers.auth.Activator;
import ru.ratauth.providers.auth.Verifier;
import ru.ratauth.providers.auth.dto.ActivateInput;
import ru.ratauth.providers.auth.dto.ActivateResult;
import ru.ratauth.providers.auth.dto.VerifyInput;
import ru.ratauth.providers.auth.dto.VerifyResult;
import rx.Observable;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ClickIdentityProvider implements IdentityProvider {

    private final DCAIdentityProvider dcaIdentityProvider;

    @Value("click")
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
        Observable<VerifyResult> verifyResult = dcaIdentityProvider.verify(input);
        verifyResult.map(r -> {
            r.setAcrValues(AcrValues.valueOf("sms"));
            return r;
        });
        return verifyResult;
    }
}

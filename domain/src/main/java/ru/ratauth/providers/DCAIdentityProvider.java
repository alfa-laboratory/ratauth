package ru.ratauth.providers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.ratauth.entities.Enroll;
import ru.ratauth.entities.IdentityProvider;
import ru.ratauth.providers.auth.Activator;
import ru.ratauth.providers.auth.ActivatorResolver;
import ru.ratauth.providers.auth.Verifier;
import ru.ratauth.providers.auth.VerifierResolver;
import ru.ratauth.providers.auth.dto.ActivateInput;
import ru.ratauth.providers.auth.dto.ActivateResult;
import ru.ratauth.providers.auth.dto.VerifyInput;
import ru.ratauth.providers.auth.dto.VerifyResult;
import rx.Observable;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DCAIdentityProvider implements IdentityProvider {

    private final ActivatorResolver activatorResolver;
    private final VerifierResolver verifierResolver;

    @Value("${ru.ratauth.idp.default}")
    private String name;

    @Override
    public String name() {
        return name;
    }

    @Override
    public Observable<ActivateResult> activate(ActivateInput input) {
        Enroll enroll = input.getEnroll();
        Activator activator = activatorResolver.find(enroll.getFirst());
        return activator.activate(input);
    }

    @Override
    public Observable<VerifyResult> verify(VerifyInput input) {
        Enroll enrolls = input.getEnroll();
        Verifier verifier = verifierResolver.find(enrolls.getFirst());
        return verifier.verify(input);
    }
}

package ru.ratauth.server.providers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ratauth.entities.Enroll;
import ru.ratauth.entities.IdentityProvider;
import ru.ratauth.providers.auth.Verifier;
import ru.ratauth.providers.auth.dto.ActivateInput;
import ru.ratauth.providers.auth.dto.ActivateResult;
import ru.ratauth.providers.auth.dto.VerifyInput;
import ru.ratauth.providers.auth.dto.VerifyResult;
import rx.Observable;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DCAIdentityProvider implements IdentityProvider {

    private final VerifierResolver verifierResolver;

    @Override
    public Observable<ActivateResult> activate(ActivateInput input) {
        return null;
    }

    @Override
    public Observable<VerifyResult> verify(VerifyInput input) {
        Enroll enrolls = input.getEnroll();
        Verifier verifier = verifierResolver.find(enrolls.getFirst());
        return verifier.verify(input);
    }
}

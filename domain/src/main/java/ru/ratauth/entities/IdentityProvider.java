package ru.ratauth.entities;

import ru.ratauth.providers.auth.dto.ActivateInput;
import ru.ratauth.providers.auth.dto.ActivateResult;
import ru.ratauth.providers.auth.dto.VerifyInput;
import ru.ratauth.providers.auth.dto.VerifyResult;
import rx.Observable;

public interface IdentityProvider {

    String name();

    Observable<ActivateResult> activate(ActivateInput input);

    Observable<VerifyResult> verify(VerifyInput input);

}

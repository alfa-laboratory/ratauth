package ru.ratauth.providers.auth;

import ru.ratauth.providers.auth.dto.ActivateInput;
import ru.ratauth.providers.auth.dto.ActivateResult;
import rx.Observable;

public interface ActivatorResolver {

    Observable<ActivateResult> activate(String enroll, ActivateInput activateInput);

}

package ru.ratauth.providers.auth;

import ru.ratauth.providers.auth.dto.ActivateInput;
import ru.ratauth.providers.auth.dto.ActivateResult;
import rx.Observable;

interface Activator {
    Observable<ActivateResult> activate(ActivateInput input);
}

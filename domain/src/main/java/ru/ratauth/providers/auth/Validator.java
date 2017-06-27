package ru.ratauth.providers.auth;

import ru.ratauth.providers.auth.dto.ValidateInput;
import ru.ratauth.providers.auth.dto.ValidateResult;
import rx.Observable;

interface Validator {
        Observable<ValidateResult> validate(ValidateInput input);
}

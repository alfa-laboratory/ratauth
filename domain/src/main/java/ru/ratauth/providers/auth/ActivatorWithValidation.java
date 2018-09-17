package ru.ratauth.providers.auth;

import ru.ratauth.providers.auth.dto.ActivateInput;
import ru.ratauth.providers.auth.dto.ActivateResult;
import rx.Observable;

public interface ActivatorWithValidation<T> {

    String name();

    T produceInputBean();

    Observable<ActivateResult> activate(T inputBean, ActivateInput input);

    String version();

}

package ru.ratauth.providers.auth;

import ru.ratauth.providers.auth.dto.VerifyInput;
import ru.ratauth.providers.auth.dto.VerifyResult;
import rx.Observable;

public interface Verifier {

    String name();

    Observable<VerifyResult> verify(VerifyInput input);

}

package ru.ratauth.providers.auth;

import ru.ratauth.providers.auth.dto.VerifierInput;
import ru.ratauth.providers.auth.dto.VerifierResult;
import rx.Observable;

interface Verifier {
        Observable<VerifierResult> verify(VerifierInput input);
}

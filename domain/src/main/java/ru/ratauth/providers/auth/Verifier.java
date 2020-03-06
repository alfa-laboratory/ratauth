package ru.ratauth.providers.auth;

import ru.ratauth.providers.auth.dto.VerifyInput;
import ru.ratauth.providers.auth.dto.VerifyResult;

public interface Verifier {

    String name();

    VerifyResult verify(VerifyInput input);

    String version();

}

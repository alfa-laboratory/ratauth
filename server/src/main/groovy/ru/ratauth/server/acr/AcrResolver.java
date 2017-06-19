package ru.ratauth.server.acr;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import ratpack.http.Request;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AcrResolver {

    private final DefaultAcrMatcher defaultAcrMatcher;
    private final MfaTokenAcrMatcher mfaTokenAcrMatcher;

    public AcrMatcher resolve(Request request) {
        return request.getQueryParams().containsKey("mfa_token") ? mfaTokenAcrMatcher : defaultAcrMatcher;
    }

}

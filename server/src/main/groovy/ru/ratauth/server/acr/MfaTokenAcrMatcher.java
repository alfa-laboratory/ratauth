package ru.ratauth.server.acr;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import ratpack.http.Request;
import ru.ratauth.server.jwt.JWTDecoder;
import ru.ratauth.server.mfatoken.MFAToken;
import ru.ratauth.server.mfatoken.MFATokenJWTConverter;

import static javaslang.Tuple.of;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MfaTokenAcrMatcher implements AcrMatcher {

    private final JWTDecoder jwtDecoder;

    @Override
    public String match(Request request) {
        return of(request, request)
                .map1(this::readAcrFromRequest)
                .map2(this::readAcrFromMfaToken)
                .transform(AcrValue::difference)
                .getAcrValues().get(0);
    }

    private AcrValue readAcrFromRequest(Request request) {
        return of(request.getQueryParams().get("acr"))
                .map(AcrValue::valueOf)
                ._1();
    }

    private AcrValue readAcrFromMfaToken(Request request) {
        return of(request.getQueryParams().get("mfa_token"))
                .map(updateToken -> jwtDecoder.decode(updateToken, new MFATokenJWTConverter()))
                .map(MFAToken::getAcrValue)
                ._1();
    }

}
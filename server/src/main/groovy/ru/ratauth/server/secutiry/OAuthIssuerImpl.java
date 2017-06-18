package ru.ratauth.server.secutiry;


import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.ratauth.server.authcode.AuthCode;
import ru.ratauth.server.authcode.AuthCodeJWTConverter;
import ru.ratauth.server.authcode.AuthCodeService;
import ru.ratauth.server.jwt.JWTSigner;
import ru.ratauth.server.scope.Scope;
import ru.ratauth.server.scope.ScopeProperties;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OAuthIssuerImpl implements OAuthIssuer {

    private final ValueGenerator vg;
    private final AuthCodeService authCodeService;
    private final ScopeProperties scopeProperties;
    private final JWTSigner jwtSigner;

    @SneakyThrows
    public String accessToken() {
        return vg.generateValue();
    }

    @SneakyThrows
    public String refreshToken() {
        return vg.generateValue();
    }

    @SneakyThrows
    public String authorizationCode() {
        Scope scope = Scope.builder()
                .scope(scopeProperties.getDefaultScope())
                .build();
        AuthCode authCode = authCodeService.createAuthCode(scope);
        return jwtSigner.createJWT(authCode, new AuthCodeJWTConverter());    }
}

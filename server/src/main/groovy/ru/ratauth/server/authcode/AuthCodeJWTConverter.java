package ru.ratauth.server.authcode;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.interfaces.DecodedJWT;
import ru.ratauth.server.jwt.JWTConverter;
import ru.ratauth.server.scope.Scope;
import ru.ratauth.server.utils.DateUtils;

import java.util.Date;

public class AuthCodeJWTConverter implements JWTConverter<AuthCode> {

    private static final String SCOPE = "scope";

    @Override
    public JWTCreator.Builder encode(AuthCode authCode) {
        Date expiresAt = DateUtils.fromLocal(authCode.getExpiresIn());

        return JWT.create()
                .withExpiresAt(expiresAt)
                .withClaim(SCOPE, authCode.getScope().toString());
    }

    @Override
    public AuthCode decode(DecodedJWT decodedJWT) {
        return AuthCode.builder()
                .expiresIn(DateUtils.toLocal(decodedJWT.getExpiresAt()))
                .scope(Scope.valueOf(decodedJWT.getClaim(SCOPE).asString()))
                .build();
    }

}

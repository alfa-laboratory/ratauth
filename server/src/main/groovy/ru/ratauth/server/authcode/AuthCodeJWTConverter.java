package ru.ratauth.server.authcode;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.interfaces.DecodedJWT;
import ru.ratauth.server.authcode.AuthCode;
import ru.ratauth.server.jwt.JWTConverter;
import ru.ratauth.server.scope.Scope;
import ru.ratauth.server.utils.DateUtils;

import java.util.Date;

public class AuthCodeJWTConverter implements JWTConverter<AuthCode> {

    @Override
    public JWTCreator.Builder convert(AuthCode authCode) {
        Date expiresAt = DateUtils.fromLocal(authCode.getExpiresIn());

        return JWT.create()
                .withExpiresAt(expiresAt)
                .withClaim("scope", authCode.getScope().toString());
    }

    @Override
    public AuthCode decode(String s) {
        DecodedJWT decoded = JWT.decode(s);
        return AuthCode.builder()
                        .scope(Scope.fromString(decoded.getClaim("scope").asString()))
                        .expiresIn(DateUtils.toLocal(decoded.getExpiresAt())).build();
    }
}

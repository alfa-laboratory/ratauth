package ru.ratauth.server.authcode;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import ru.ratauth.server.authcode.AuthCode;
import ru.ratauth.server.jwt.JWTConverter;
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
}

package ru.ratauth.server.mfatoken;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.interfaces.DecodedJWT;
import ru.ratauth.server.acr.AcrValue;
import ru.ratauth.server.jwt.JWTConverter;
import ru.ratauth.server.scope.Scope;
import ru.ratauth.server.utils.DateUtils;

public class MFATokenJWTConverter implements JWTConverter<MFAToken> {

    private static final String ID = "id";
    private static final String ACR = "acr";
    private static final String SCOPE = "scope";

    @Override
    public JWTCreator.Builder convert(MFAToken mfaToken) {
        return JWT.create()
                .withClaim(ID, mfaToken.getId())
                .withExpiresAt(DateUtils.fromLocal(mfaToken.getExpiredAt()))
                .withClaim(ACR, mfaToken.getAcrValue().toString())
                .withClaim(SCOPE, mfaToken.getScope().toString());
    }

    @Override
    public MFAToken decode(String token) {
        DecodedJWT decodedJWT = JWT.decode(token);
        return MFAToken.builder()
                .id(decodedJWT.getClaim(ID).asString())
                .expiredAt(DateUtils.toLocal(decodedJWT.getExpiresAt()))
                .acrValue(AcrValue.valueOf(decodedJWT.getClaim(ACR).asString()))
                .scope(Scope.valueOf(decodedJWT.getClaim(SCOPE).asString()))
                .build();
    }
}

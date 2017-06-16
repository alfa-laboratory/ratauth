package ru.ratauth.server.accesstoken;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.interfaces.DecodedJWT;
import ru.ratauth.server.acr.AcrValue;
import ru.ratauth.server.jwt.JWTConverter;
import ru.ratauth.server.scope.Scope;
import ru.ratauth.server.utils.DateUtils;

public class AccessTokenJWTConverter implements JWTConverter<AccessToken> {

    private static final String ID = "id";
    private static final String ACR = "acr";
    private static final String SCOPE = "scope";

    @Override
    public JWTCreator.Builder decode(AccessToken accessToken) {
        return JWT.create()
                .withClaim(ID, accessToken.getId())
                .withExpiresAt(DateUtils.fromLocal(accessToken.getExpiredAt()))
                .withClaim(ACR, accessToken.getAcrValue().toString())
                .withClaim(SCOPE, accessToken.getScope().toString());
    }

    @Override
    public AccessToken encode(DecodedJWT decodedJWT) {
        return AccessToken.builder()
                .id(decodedJWT.getClaim(ID).asString())
                .expiredAt(DateUtils.toLocal(decodedJWT.getExpiresAt()))
                .acrValue(AcrValue.valueOf(decodedJWT.getClaim(ACR).asString()))
                .scope(Scope.valueOf(decodedJWT.getClaim(SCOPE).asString()))
                .build();
    }
}

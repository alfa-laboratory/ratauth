package ru.ratauth.server.jwt;

import com.auth0.jwt.interfaces.DecodedJWT;

import static com.auth0.jwt.JWTCreator.Builder;

public interface JWTConverter<T> {

    Builder encode(T t);

    T decode(DecodedJWT decodedJWT);

}

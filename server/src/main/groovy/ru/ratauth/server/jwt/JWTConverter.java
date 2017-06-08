package ru.ratauth.server.jwt;

import static com.auth0.jwt.JWTCreator.Builder;

public interface JWTConverter<T> {

    Builder convert(T t);

}
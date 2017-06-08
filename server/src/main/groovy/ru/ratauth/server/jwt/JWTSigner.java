package ru.ratauth.server.jwt;

public interface JWTSigner {

    <T, S extends JWTConverter<T>> String createJWT(T object, S jwtConverter);

}

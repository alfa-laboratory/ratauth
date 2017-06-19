package ru.ratauth.server.jwt;

public interface JWTDecoder {

    <T, S extends JWTConverter<T>> T verify(String token, S jwtConverter);

    <T, S extends JWTConverter<T>> T decode(String token, S jwtConverter);

}

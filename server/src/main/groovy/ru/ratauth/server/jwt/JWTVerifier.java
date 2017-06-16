package ru.ratauth.server.jwt;

public interface JWTVerifier {

    <T, S extends JWTConverter<T>> T verify(String token, S jwtConverter);

}

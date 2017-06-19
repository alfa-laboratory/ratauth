package ru.ratauth.server.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Verification;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

import static javaslang.Tuple.of;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class HMAC256JWTDecoder implements JWTDecoder {

    @Autowired
    private final Algorithm algorithm;

    @Override
    public <T, S extends JWTConverter<T>> T verify(String token, S jwtConverter) {
        return of(algorithm)
                .map(JWT::require)
                .map(Verification::build)
                .map(jwtVerifier -> jwtVerifier.verify(token))
                .map(jwtConverter::decode)
                ._1();
    }

    @Override
    public <T, S extends JWTConverter<T>> T decode(String token, S jwtConverter) {
        return of(token)
                .map(JWT::decode)
                .map(jwtConverter::decode)
                ._1();
    }
}

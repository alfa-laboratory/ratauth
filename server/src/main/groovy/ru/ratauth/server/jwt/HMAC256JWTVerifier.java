package ru.ratauth.server.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class HMAC256JWTVerifier implements JWTVerifier {

    @Autowired
    private final Algorithm algorithm;

    @Override
    @SneakyThrows
    public <T, S extends JWTConverter<T>> T verify(String token, S jwtConverter) {

        DecodedJWT jwt = JWT.require(algorithm).build().verify(token);

//        DecodedJWT jwt = verifier.verify(token);
        return jwtConverter.encode(jwt);
    }
}

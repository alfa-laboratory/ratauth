package ru.ratauth.server.jwt;

import com.auth0.jwt.algorithms.Algorithm;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class HMAC256JWTSigner implements JWTSigner {

    private final JWTProperties jwtProperties;
    private final Algorithm algorithm;

    @Override
    @SneakyThrows
    public <T, S extends JWTConverter<T>> String createJWT(T object, S jwtConverter) {
        String issuer = jwtProperties.getIssuer();

        return jwtConverter.convert(object)
                .withIssuer(issuer)
                .sign(algorithm);
    }

    @Override
    public <T, S extends JWTConverter<T>> T parseJWT(String object, S jwtConverter) {
        return jwtConverter.decode(object);
    }


}

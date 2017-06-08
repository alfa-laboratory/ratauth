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

    @SneakyThrows
    public <T, S extends JWTConverter<T>> String createJWT(T object, S jwtConverter) {
        Algorithm algorithm = Algorithm.HMAC256(jwtProperties.getSecret());
        String issuer = jwtProperties.getIssuer();

        return jwtConverter.convert(object)
                .withIssuer(issuer)
                .sign(algorithm);
    }

}

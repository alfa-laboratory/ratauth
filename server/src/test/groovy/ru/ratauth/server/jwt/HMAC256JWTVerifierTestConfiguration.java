package ru.ratauth.server.jwt;

import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@TestConfiguration
@Import({JWTConfiguration.class, JWTPropertiesTestConfiguration.class})
public class HMAC256JWTVerifierTestConfiguration {

    @Bean
    public JWTDecoder jwtVerifier(Algorithm algorithm) {
        return new HMAC256JWTDecoder(algorithm);
    }

}

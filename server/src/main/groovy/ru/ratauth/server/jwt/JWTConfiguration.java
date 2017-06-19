package ru.ratauth.server.jwt;

import com.auth0.jwt.algorithms.Algorithm;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@EnableConfigurationProperties(JWTProperties.class)
public class JWTConfiguration {

    @Autowired
    private final JWTProperties jwtProperties;

    @Bean
    @SneakyThrows
    public Algorithm algorithm() {
        return Algorithm.HMAC256(jwtProperties.getSecret());
    }

    @Bean
    public JWTDecoder jwtDecoder() {
        return new HMAC256JWTDecoder(algorithm());
    }

}

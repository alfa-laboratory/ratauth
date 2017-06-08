package ru.ratauth.server.jwt;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import ru.ratauth.server.authcode.AuthCode;
import ru.ratauth.server.authcode.AuthCodeJWTConverter;
import ru.ratauth.server.scope.Scope;

import java.time.LocalDateTime;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = RSA256JWTSignerTest.RSA256JWTSignerTestConfiguration.class, initializers = ConfigFileApplicationContextInitializer.class)
public class RSA256JWTSignerTest {

    @Autowired
    private JWTSigner jwtSigner;

    @Test
    public void createJWT() throws Exception {

        AuthCode authCode = createAuthCode();

        String jwt = jwtSigner.createJWT(authCode, new AuthCodeJWTConverter());

        System.out.println(jwt);

    }

    private AuthCode createAuthCode() {
        LocalDateTime expiresIn = LocalDateTime.of(2017, 8, 1, 0, 0);

        Scope scope = Scope.builder()
                .scope("read")
                .scope("write")
                .build();

        return AuthCode.builder()
                .expiresIn(expiresIn)
                .scope(scope)
                .build();
    }

    @TestConfiguration
    @EnableConfigurationProperties(JWTProperties.class)
    public static class RSA256JWTSignerTestConfiguration {

        @Bean
        public JWTSigner jwtSigner(JWTProperties jwtProperties) {
            return new RSA256JWTSigner(jwtProperties);
        }

    }
}
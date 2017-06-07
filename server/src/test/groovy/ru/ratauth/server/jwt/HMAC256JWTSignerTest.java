package ru.ratauth.server.jwt;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import ru.ratauth.server.authcode.AuthCode;
import ru.ratauth.server.authcode.AuthCodeJWTConverter;
import ru.ratauth.server.scope.Scope;

import java.time.LocalDateTime;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = HMAC256JWTSignerTest.HMAC256JWTSignerTestConfiguration.class)
public class HMAC256JWTSignerTest {

    private static final String AUTH_CODE = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzY29wZSI6InJlYWQ6d3JpdGUiLCJpc3MiOiJhbGZhLWJhbmsiLCJleHAiOjE0OTM1ODYwMDB9.g0IhAKWL41UJzyCb8P34p3RqYMHuiv6MlN4q6AK0ctg";

    @MockBean
    private JWTProperties jwtProperties;

    @Autowired
    private JWTSigner jwtSigner;

    @Before
    public void setUp() {
        when(jwtProperties.getIssuer()).thenReturn("alfa-bank");
        when(jwtProperties.getSecret()).thenReturn("secret");
    }

    @Test
    public void testCreateJWT() throws Exception {

        LocalDateTime expiresIn = LocalDateTime.of(2017, 5, 1, 0, 0);

        Scope scope = Scope.builder()
                .scope("read")
                .scope("write")
                .build();

        AuthCode authCode = AuthCode.builder()
                .expiresIn(expiresIn)
                .scope(scope)
                .build();

        String signedAuthCode = jwtSigner.createJWT(authCode, new AuthCodeJWTConverter());

        assertEquals(AUTH_CODE, signedAuthCode);
    }

    @TestConfiguration
    public static class HMAC256JWTSignerTestConfiguration {

        @Bean
        public JWTSigner jwtSigner(JWTProperties jwtProperties) {
            return new HMAC256JWTSigner(jwtProperties);
        }

    }

}

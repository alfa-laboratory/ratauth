package ru.ratauth.server.jwt;

import org.apache.commons.codec.binary.Base64;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import ru.ratauth.server.authcode.AuthCode;
import ru.ratauth.server.authcode.AuthCodeJWTConverter;
import ru.ratauth.server.scope.Scope;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static com.jayway.restassured.path.json.JsonPath.with;
import static junit.framework.TestCase.assertEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNull.notNullValue;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = HMAC256JWTSignerTestConfiguration.class)
public class HMAC256JWTSignerTest {

    @Autowired
    private JWTSigner jwtSigner;

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

        assertThat(signedAuthCode, is(notNullValue()));

        String[] parts = signedAuthCode.split("\\.");
        String bodyJson = new String(Base64.decodeBase64(parts[1]), StandardCharsets.UTF_8);

        assertEquals("read:write", with(bodyJson).getString("scope"));
        assertEquals("alfa-bank", with(bodyJson).getString("iss"));
    }

}

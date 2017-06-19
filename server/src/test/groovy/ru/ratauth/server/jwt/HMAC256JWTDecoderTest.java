package ru.ratauth.server.jwt;

import com.auth0.jwt.exceptions.TokenExpiredException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import ru.ratauth.server.authcode.AuthCodeJWTConverter;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = HMAC256JWTVerifierTestConfiguration.class)
public class HMAC256JWTDecoderTest {

    private static final String AUTH_CODE = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzY29wZSI6InJlYWQ6d3JpdGUiLCJpc3MiOiJhbGZhLWJhbmsiLCJleHAiOjE0OTM1ODYwMDB9.g0IhAKWL41UJzyCb8P34p3RqYMHuiv6MlN4q6AK0ctg";

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Autowired
    private JWTDecoder jwtDecoder;

    @Before
    public void setUp() {

    }

    @Test
    public void testVerifyExpired() throws Exception {
        expectedException.expect(TokenExpiredException.class);
        expectedException.expectMessage("The Token has expired");
        jwtDecoder.verify(AUTH_CODE, new AuthCodeJWTConverter());
    }


    @Test
    public void testVerify() throws Exception {
        expectedException.expect(TokenExpiredException.class);
        expectedException.expectMessage("The Token has expired");
        jwtDecoder.verify(AUTH_CODE, new AuthCodeJWTConverter());
    }

}

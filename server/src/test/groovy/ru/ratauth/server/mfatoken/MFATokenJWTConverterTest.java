package ru.ratauth.server.mfatoken;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import ru.ratauth.server.acr.AcrValue;
import ru.ratauth.server.jwt.HMAC256JWTSignerTestConfiguration;
import ru.ratauth.server.jwt.JWTConverter;
import ru.ratauth.server.jwt.JWTDecoder;
import ru.ratauth.server.jwt.JWTSigner;
import ru.ratauth.server.scope.Scope;

import java.time.LocalDateTime;

import static junit.framework.TestCase.assertEquals;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = MFATokenJWTConverterTest.MFATokenJWTConverterTestConfiguration.class)
public class MFATokenJWTConverterTest {

    private static final String MFA_TOKEN = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJhY3IiOiJjYXJkIiwic2NvcGUiOiJtb2JpbGUucmVhZCIsImlzcyI6ImFsZmEtYmFuayIsImlkIjoiMSIsImV4cCI6OTQ2Njc0MDAwfQ.VUDADpmnC9NDO6UBUREWjUFe_fisMTWVdfWw4TgvNFg";

    @Autowired
    private JWTSigner jwtSigner;

    @Autowired
    private JWTDecoder jwtDecoder;

    @Autowired
    private JWTConverter<MFAToken> mfaTokenJWTConverter;
    private MFAToken mfaToken;

    @Before
    public void setUp() {
        mfaToken = MFAToken.builder()
                .id("1")
                .acrValue(AcrValue.valueOf("card"))
                .scope(Scope.valueOf("mobile.read"))
                .expiredAt(LocalDateTime.of(2000, 1, 1, 0, 0))
                .build();
    }

    @Test
    public void encode() throws Exception {
        String jwt = jwtSigner.createJWT(mfaToken, mfaTokenJWTConverter);
        System.out.println(jwt);
    }

    @Test
    public void decode() throws Exception {
        MFAToken resultMfaToken = jwtDecoder.decode(MFA_TOKEN, mfaTokenJWTConverter);
        assertEquals(mfaToken, resultMfaToken);
    }

    @TestConfiguration
    @Import(HMAC256JWTSignerTestConfiguration.class)
    public static class MFATokenJWTConverterTestConfiguration {

        @Bean
        public JWTConverter<MFAToken> mfaTokenJWTConverter() {
            return new MFATokenJWTConverter();
        }

    }

}
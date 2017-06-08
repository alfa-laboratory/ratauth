package ru.ratauth.server.authcode;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import ru.ratauth.server.scope.Scope;

import java.time.LocalDateTime;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = AuthCodeServiceTest.AuthCodeServiceTestConfiguration.class)
public class AuthCodeServiceTest {

    private static final String SCOPE = "mobile.read";
    private static final long EXPIRES_IN_SECOND = 120L;

    @MockBean
    private AuthCodeProperties authCodeProperties;

    @Autowired
    private AuthCodeService authCodeService;

    @Before
    public void setUp() {
        when(authCodeProperties.getExpiresInSecond()).thenReturn(EXPIRES_IN_SECOND);
    }

    @Test
    public void createAuthCode() throws Exception {

        Scope scope = Scope.builder()
                .scope(SCOPE)
                .build();

        AuthCode authCode = authCodeService.createAuthCode(scope);

        assertEquals(SCOPE, authCode.getScope().toString());
        assertNotNull(authCode.getExpiresIn());
    }

    @TestConfiguration
    public static class AuthCodeServiceTestConfiguration {

        @Bean
        public AuthCodeService authCodeService(AuthCodeProperties authCodeProperties) {
            return new AuthCodeServiceImpl(authCodeProperties);
        }
    }

}
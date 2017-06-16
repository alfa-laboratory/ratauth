package ru.ratauth.server.authcode;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import ru.ratauth.server.scope.Scope;

import java.time.LocalDateTime;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = AuthCodeServiceTestConfiguration.class)
public class AuthCodeServiceImplTest {

    private static final String SCOPE = "mobile.read";
    private static final long EXPIRES_IN_SECOND = 120L;
    private static final String SCOPE_MOBILE_READ = "mobile.read";

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
                .scope(SCOPE_MOBILE_READ)
                .build();

        AuthCode authCode = authCodeService.createAuthCode(scope);

        assertEquals(SCOPE, authCode.getScope().toString());
        assertEquals(LocalDateTime.of(2000, 1, 1, 0, 2), authCode.getExpiresIn());
    }

}

package ru.ratauth.inmemory.ip.providers.domain;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import ru.ratauth.inmemory.ip.providers.util.LoginGenerator;
import ru.ratauth.inmemory.ip.providers.util.SMSGenerator;
import ru.ratauth.providers.registrations.dto.RegInput;

import java.util.HashMap;
import java.util.Map;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static ru.ratauth.providers.auth.dto.BaseAuthFields.PASSWORD;
import static ru.ratauth.providers.auth.dto.BaseAuthFields.USERNAME;

public class UserFactoryTest {

    private static final String USER_NAME = "cotton";
    private static final int SMS_CODE = 9999;

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    private UserFactory userFactory;

    @Before
    public void setUp() {
        SMSGenerator smsGenerator = mock(SMSGenerator.class);
        when(smsGenerator.generateSMSCode()).thenReturn(SMS_CODE);

        LoginGenerator loginGenerator = mock(LoginGenerator.class);
        when(loginGenerator.generateLogin()).thenReturn(USER_NAME);

        userFactory = new UserFactory(smsGenerator, loginGenerator);
    }

    @Test
    public void testCreateDefaultUserWithUserNameAndPasswordFromRegistrationInput() {

        Map<String, String> data = new HashMap<>();
        data.put(USERNAME.val(), "cotton");
        data.put(PASSWORD.val(), "password");

        RegInput regInput = new RegInput();
        regInput.setData(data);

        User user = userFactory.createDefaultUser(regInput);

        assertEquals("cotton", user.getUserName());
        assertEquals("cotton", user.getUserId());
        assertEquals("password", user.getPassword());
        assertEquals(Integer.toString(SMS_CODE), user.getCode());
    }

    @Test
    public void testCreateDefaultUserWithGeneratedUserNameAndPassword() {
        RegInput regInput = new RegInput();
        regInput.setData(new HashMap<>());

        User user = userFactory.createDefaultUser(regInput);

        assertEquals("cotton", user.getUserName());
        assertEquals("cotton", user.getUserId());
        assertEquals(Integer.toString(SMS_CODE), user.getCode());
    }

    @Test
    public void testCreateDefaultUserThrowNPEWhenDataDoesnotExist() {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("Registration input must contains data");

        RegInput regInput = new RegInput();

        userFactory.createDefaultUser(regInput);
    }

}
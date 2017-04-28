package ru.ratauth.inmemory.ip.providers.registration;

import org.junit.Before;
import org.junit.Test;
import ru.ratauth.inmemory.ip.providers.domain.User;
import ru.ratauth.inmemory.ip.providers.domain.UserFactory;
import ru.ratauth.inmemory.ip.providers.util.LoginGenerator;
import ru.ratauth.inmemory.ip.providers.util.SMSGenerator;
import ru.ratauth.providers.registrations.dto.RegInput;
import ru.ratauth.providers.registrations.dto.RegResult;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static ru.ratauth.providers.registrations.dto.RegResult.Status.NEED_APPROVAL;

public class CardNumberRegistrationSupportTest {

    private static final String USER_NAME_FIELD = "username";
    private static final String CODE_FIELD = "code";
    private static final String CARD_NUMBER_FIELD = "credential[cardNumber]";
    private static final String CARD_MONTH_FIELD = "credential[month]";
    private static final String CARD_YEAR_FIELD = "credential[year]";
    private static final String PHONE_NUMBER_FIELD = "credential[phoneNumber]";

    private static final String CARD_NUMBER = "4154 8120 2967 7972";
    private static final String CARD_MONTH = "02";
    private static final String CARD_YEAR = "19";
    private static final String PHONE_NUMBER = "79151234565";
    private static final String USER_NAME = "cotton";
    private static final int SMS_CODE = 9999;

    private CardNumberRegistrationSupport registrationSupport;

    @Before
    public void setUp() {

        SMSGenerator smsGenerator = mock(SMSGenerator.class);
        when(smsGenerator.generateSMSCode()).thenReturn(SMS_CODE);

        LoginGenerator loginGenerator = mock(LoginGenerator.class);
        when(loginGenerator.generateLogin()).thenReturn(USER_NAME);

        UserFactory userFactory = new UserFactory(smsGenerator, loginGenerator);

        registrationSupport = new CardNumberRegistrationSupport(userFactory);
    }

    @Test
    public void testIsResponsible() throws Exception {
        RegInput regInput = createRegInput();
        boolean isResponsible = registrationSupport.isResponsible(regInput);

        assertTrue(isResponsible);
    }

    @Test
    public void testRegister() throws Exception {
        RegInput regInput = createRegInput();
        User user = registrationSupport.register(regInput);

        assertEquals(4, user.getOptionalData().size());
        assertEquals(CARD_NUMBER, user.getField(CARD_NUMBER_FIELD));
        assertEquals(CARD_MONTH, user.getField(CARD_MONTH_FIELD));
        assertEquals(CARD_YEAR, user.getField(CARD_YEAR_FIELD));
        assertEquals(PHONE_NUMBER, user.getField(PHONE_NUMBER_FIELD));

        assertEquals(USER_NAME, user.getUserName());
        assertEquals(USER_NAME, user.getUserId());
        assertEquals(Integer.toString(SMS_CODE), user.getCode());
    }

    @Test
    public void testToRegResult() throws Exception {

        User user = User.builder()
                .userName(USER_NAME)
                .code(Integer.toString(SMS_CODE))
                .build();

        RegResult regResult = registrationSupport.toRegResult(user);

        assertEquals(2, regResult.getData().size());
        assertEquals(NEED_APPROVAL, regResult.getStatus());
        assertEquals(USER_NAME, regResult.getData().get(USER_NAME_FIELD));
        assertEquals(SMS_CODE, Integer.parseInt((String) regResult.getData().get(CODE_FIELD)));
    }

    private RegInput createRegInput() {
        Map<String, String> map = new HashMap<>();
        map.put(CARD_NUMBER_FIELD, CARD_NUMBER);
        map.put(CARD_MONTH_FIELD, CARD_MONTH);
        map.put(CARD_YEAR_FIELD, CARD_YEAR);
        map.put(PHONE_NUMBER_FIELD, PHONE_NUMBER);
        RegInput regInput = new RegInput();
        regInput.setData(map);
        return regInput;
    }

}
package ru.ratauth.inmemory.ip.providers.registration;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.ratauth.inmemory.ip.providers.domain.User;
import ru.ratauth.inmemory.ip.providers.domain.UserFactory;
import ru.ratauth.providers.registrations.dto.RegInput;
import ru.ratauth.providers.registrations.dto.RegResult;

import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;
import static ru.ratauth.providers.auth.dto.BaseAuthFields.CODE;
import static ru.ratauth.providers.auth.dto.BaseAuthFields.USERNAME;
import static ru.ratauth.providers.registrations.dto.RegResult.Status.NEED_APPROVAL;

@Component
@RequiredArgsConstructor
public class CardNumberRegistrationSupport implements RegistrationSupport {

    private static final String CARD_NUMBER = "credential[cardNumber]";
    private static final String CARD_MONTH = "credential[month]";
    private static final String CARD_YEAR = "credential[year]";
    private static final String PHONE_NUMBER = "credential[phoneNumber]";
    private final UserFactory userFactory;

    @Override
    public boolean isResponsible(RegInput regInput) {
        return regInput.getData().containsKey(CARD_NUMBER)
                && regInput.getData().containsKey(CARD_MONTH)
                && regInput.getData().containsKey(CARD_YEAR)
                && regInput.getData().containsKey(PHONE_NUMBER);
    }

    @Override
    public User register(RegInput regInput) {
        User user = userFactory.createDefaultUser(regInput);

        String cardNumber = regInput.getData().get(CARD_NUMBER);
        String cardMonth = regInput.getData().get(CARD_MONTH);
        String cardYear = regInput.getData().get(CARD_YEAR);
        String phoneNumber = regInput.getData().get(PHONE_NUMBER);

        requireNonNullField(cardNumber, cardMonth, cardYear, phoneNumber);

        user = user.putValue(CARD_NUMBER, cardNumber);
        user = user.putValue(CARD_MONTH, cardMonth);
        user = user.putValue(CARD_YEAR, cardYear);
        user = user.putValue(PHONE_NUMBER, phoneNumber);

        return user;
    }

    @Override
    public RegResult toRegResult(User user) {
        Map<String, Object> registrationResultData = new HashMap<>();
        registrationResultData.put(USERNAME.val(), user.getUserName());
        registrationResultData.put(CODE.val(), user.getCode());

        return RegResult.builder()
                .data(registrationResultData)
                .status(NEED_APPROVAL)
                .build();
    }

    private void requireNonNullField(String cardNumber, String cardMonth, String cardYear, String phoneNumber) {
        requireNonNull(cardNumber, "Card number can not be null");
        requireNonNull(cardMonth, "Card month can not be null");
        requireNonNull(cardYear, "Card year can not be null");
        requireNonNull(phoneNumber, "Phone number can not be null");
    }
}

package ru.ratauth.inmemory.ip.providers.registration;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ratauth.inmemory.ip.providers.domain.User;
import ru.ratauth.inmemory.ip.providers.domain.UserFactory;
import ru.ratauth.providers.registrations.dto.RegInput;
import ru.ratauth.providers.registrations.dto.RegResult;

import java.util.HashMap;
import java.util.Map;

import static ru.ratauth.providers.auth.dto.BaseAuthFields.*;
import static ru.ratauth.providers.registrations.dto.RegResult.Status.SUCCESS;
import static ru.ratauth.providers.registrations.dto.RegResult.builder;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UserNameRegistrationSupport implements RegistrationSupport {

    private final UserFactory userFactory;

    @Override
    public boolean isResponsible(RegInput regInput) {
        return regInput.getData().get(USERNAME.val()) != null;
    }

    @Override
    public User register(RegInput regInput) {

        User user = userFactory.createDefaultUser(regInput);

        Map<String, Object> registrationResultData = new HashMap<>();
        registrationResultData.put(USER_ID.val(), user.getUserId());
        registrationResultData.put(CODE.val(), user.getCode());

        return user;
    }

    @Override
    public RegResult toRegResult(User user) {
        Map<String, Object> registrationResultData = new HashMap<>();
        registrationResultData.put(USER_ID.val(), user.getUserId());
        registrationResultData.put(CODE.val(), user.getCode());

        return builder()
                .data(registrationResultData)
                .status(SUCCESS)
                .build();
    }

}
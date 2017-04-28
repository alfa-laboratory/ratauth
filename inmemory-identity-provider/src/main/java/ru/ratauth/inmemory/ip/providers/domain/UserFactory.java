package ru.ratauth.inmemory.ip.providers.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ratauth.inmemory.ip.providers.util.LoginGenerator;
import ru.ratauth.inmemory.ip.providers.util.SMSGenerator;
import ru.ratauth.providers.registrations.dto.RegInput;

import java.util.Map;

import static ru.ratauth.providers.auth.dto.BaseAuthFields.PASSWORD;
import static ru.ratauth.providers.auth.dto.BaseAuthFields.USERNAME;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UserFactory {

    private final SMSGenerator smsGenerator;
    private final LoginGenerator loginGenerator;

    public User createDefaultUser(RegInput regInput) {

        Map<String, String> data = regInput.getData();
        if (data == null) {
            throw new NullPointerException("Registration input must contains data");
        }

        String userName = data.get(USERNAME.val());
        String password = data.get(PASSWORD.val());

        String finalUserName = userName == null ? loginGenerator.generateLogin() : userName;
        return User.builder()
                .userName(finalUserName)
                .userId(finalUserName)
                .password(password)
                .code(Integer.toString(smsGenerator.generateSMSCode()))
                .build();
    }

}

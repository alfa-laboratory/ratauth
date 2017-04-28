package ru.ratauth.inmemory.ip.providers.registration;

import ru.ratauth.inmemory.ip.providers.domain.User;
import ru.ratauth.providers.registrations.dto.RegInput;
import ru.ratauth.providers.registrations.dto.RegResult;

public interface RegistrationSupport {

    boolean isResponsible(RegInput regInput);

    User register(RegInput regInput);

    RegResult toRegResult(User user);

}

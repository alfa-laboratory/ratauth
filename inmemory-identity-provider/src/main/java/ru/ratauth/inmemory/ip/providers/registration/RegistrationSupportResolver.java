package ru.ratauth.inmemory.ip.providers.registration;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ratauth.providers.registrations.dto.RegInput;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RegistrationSupportResolver {

    private final List<RegistrationSupport> registrationList;

    public RegistrationSupport findRegistrationSupport(RegInput regInput) {
        List<RegistrationSupport> registrations = registrationList.stream()
                .filter(registration -> registration.isResponsible(regInput))
                .collect(toList());

        if (registrations.size() == 0) {
            throw new IllegalArgumentException("There is no any registration support for such input data");
        }

        if (registrations.size() > 1) {
            throw new IllegalArgumentException("There are many registration support for such input data");
        }

        return registrations.get(0);
    }

}

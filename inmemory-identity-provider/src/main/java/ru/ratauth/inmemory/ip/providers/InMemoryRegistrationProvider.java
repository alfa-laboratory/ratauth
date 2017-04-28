package ru.ratauth.inmemory.ip.providers;

import lombok.RequiredArgsConstructor;
import ru.ratauth.exception.RegistrationException;
import ru.ratauth.inmemory.ip.providers.domain.User;
import ru.ratauth.inmemory.ip.providers.domain.UserFactory;
import ru.ratauth.inmemory.ip.providers.registration.RegistrationSupport;
import ru.ratauth.inmemory.ip.providers.registration.RegistrationSupportResolver;
import ru.ratauth.inmemory.ip.providers.repository.UserRepository;
import ru.ratauth.providers.registrations.RegistrationProvider;
import ru.ratauth.providers.registrations.dto.RegInput;
import ru.ratauth.providers.registrations.dto.RegResult;
import rx.Observable;

import java.util.HashMap;
import java.util.Map;

import static ru.ratauth.providers.auth.dto.BaseAuthFields.*;
import static ru.ratauth.providers.registrations.dto.RegResult.builder;
import static rx.Observable.error;
import static rx.Observable.just;

@RequiredArgsConstructor
public class InMemoryRegistrationProvider implements RegistrationProvider {

    private final UserRepository userRepository;
    private final UserFactory userFactory;
    private final RegistrationSupportResolver registrationSupportResolver;

    @Override
    public Observable<RegResult> register(RegInput regInput) {

        if (isFirstStepOfRegistration(regInput)) {
            RegistrationSupport registrationSupport = registrationSupportResolver.findRegistrationSupport(regInput);
            User user = registrationSupport.register(regInput);
            userRepository.save(user);
            RegResult regResult = registrationSupport.toRegResult(user);
            return just(regResult);
        } else {
            String userName = regInput.getData().get(USERNAME.val());
            User user = userRepository.getByUserName(userName);
            if (isSecondStepOfRegistration(regInput, user)) {
                return just(secondStepOfRegistration(user));
            }
        }
        return error(new RegistrationException("RegistrationSupport failed"));
    }

    @Override
    public boolean isRegCodeSupported() {
        return true;
    }

    private boolean isFirstStepOfRegistration(RegInput regInput) {
        return !regInput.getData().containsKey(CODE.val());
    }

    private boolean isSecondStepOfRegistration(RegInput regInput, User user) {
        String userName = regInput.getData().get(USERNAME.val());
        String code = regInput.getData().get(CODE.val());
        return (code.equals(user.getCode())) && (userName.equals(user.getUserName()));
    }

    private RegResult secondStepOfRegistration(User user) {

        Map<String, Object> map = new HashMap<>();
        map.put(USER_ID.val(), user.getUserId());

        return builder()
                .redirectUrl("http://relying.party/gateway")
                .data(map)
                .status(RegResult.Status.SUCCESS)
                .build();
    }

}
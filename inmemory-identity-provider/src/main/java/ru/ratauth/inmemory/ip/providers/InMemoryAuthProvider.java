package ru.ratauth.inmemory.ip.providers;

import lombok.RequiredArgsConstructor;
import ru.ratauth.exception.AuthorizationException;
import ru.ratauth.inmemory.ip.providers.domain.User;
import ru.ratauth.inmemory.ip.providers.repository.UserRepository;
import ru.ratauth.providers.auth.AuthProvider;
import ru.ratauth.providers.auth.dto.AuthInput;
import ru.ratauth.providers.auth.dto.AuthResult;
import rx.Observable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static java.lang.String.format;
import static ru.ratauth.exception.AuthorizationException.ID.CREDENTIALS_WRONG;
import static ru.ratauth.providers.auth.dto.AuthResult.Status.SUCCESS;
import static ru.ratauth.providers.auth.dto.BaseAuthFields.*;
import static rx.Observable.error;
import static rx.Observable.just;

@RequiredArgsConstructor
public class InMemoryAuthProvider implements AuthProvider {

    private final UserRepository userRepository;

    @Override
    public Observable<AuthResult> authenticate(AuthInput input) {

        String userName = input.getData().get(USERNAME.val());
        String password = input.getData().get(PASSWORD.val());

        Objects.requireNonNull(userName, "User name can not be null");
        Objects.requireNonNull(password, "Password can not be null");

        User user = userRepository.getByUserName(userName);

        if (user == null) {
            throw new IllegalArgumentException(format("User with such username: %s doesn't exist", userName));
        }

        if (isAuthenticated(userName, password, user)) {
            return makeRegResult(user);
        } else {
            return error(new AuthorizationException(CREDENTIALS_WRONG));
        }
    }


    @Override
    public boolean isAuthCodeSupported() {
        return false;
    }

    @Override
    public Observable<Boolean> checkUserStatus(AuthInput input) {
        return just(true);
    }

    private boolean isAuthenticated(String userName, String password, User user) {
        return (userName.equals(user.getUserName())) && (password.equals(user.getPassword()));
    }

    private Observable<AuthResult> makeRegResult(User user) {
        Map<String, Object> map = new HashMap<>();
        map.put(USER_ID.val(), user.getUserId());
        return just(AuthResult.builder()
                .data(map)
                .status(SUCCESS)
                .build());
    }

}
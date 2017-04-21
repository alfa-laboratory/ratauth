package ru.ratauth.inmemory.ip.providers;

import ru.ratauth.exception.AuthorizationException;
import ru.ratauth.exception.RegistrationException;
import ru.ratauth.providers.auth.AuthProvider;
import ru.ratauth.providers.auth.dto.AuthInput;
import ru.ratauth.providers.auth.dto.AuthResult;
import ru.ratauth.providers.auth.dto.BaseAuthFields;
import ru.ratauth.providers.registrations.RegistrationProvider;
import ru.ratauth.providers.registrations.dto.RegInput;
import ru.ratauth.providers.registrations.dto.RegResult;
import rx.Observable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.*;
import static java.util.stream.Collectors.toMap;
import static ru.ratauth.providers.registrations.dto.RegResult.*;
import static ru.ratauth.providers.registrations.dto.RegResult.Status.*;

public class InMemoryAuthProvider implements AuthProvider, RegistrationProvider {

    private static final String USER_NAME_FIELD = BaseAuthFields.USERNAME.val();
    private static final String PASSWORD_FIELD = BaseAuthFields.PASSWORD.val();
    private static final String USER_ID_FIELD = BaseAuthFields.USER_ID.val();
    private static final String CODE_FIELD = BaseAuthFields.CODE.val();
    private static final String CREDENTIAL = "credential";
    private static final String REG_CODE = "123";
    private final Map<String, User> users;

    public InMemoryAuthProvider(List<User> users) {
        this.users = users.stream().collect(toMap(k -> k.getUserName(), k -> k));
    }

    @Override
    public Observable<AuthResult> authenticate(AuthInput input) {
        String userName = input.getData().get(USER_NAME_FIELD);
        String password = input.getData().get(PASSWORD_FIELD);

        User user = users.get(userName);

        if (user == null) {
            throw new IllegalArgumentException(format("User with such login: %s doesn't exist", userName));
        }

        if (password.equals(user.getPassword())) {
            HashMap<String, String> map = new HashMap<>();
            map.put(USER_ID_FIELD, user.getUserId());
            return Observable.just(AuthResult.builder()
                    .data(map)
                    .status(AuthResult.Status.SUCCESS)
                    .build());
        } else {
            return Observable.error(new AuthorizationException(AuthorizationException.ID.CREDENTIALS_WRONG));
        }
    }

    @Override
    public boolean isAuthCodeSupported() {
        return false;
    }

    @Override
    public Observable<Boolean> checkUserStatus(AuthInput input) {
        return Observable.just(true);
    }

    @Override
    public Observable<RegResult> register(RegInput input) {
        String userName = input.getData().get(USER_NAME_FIELD);
        String password = input.getData().get(PASSWORD_FIELD);

        User user = users.get(userName);


        if (isFirstStep(input)) {
            if (isRegisterByLogin(userName, password, user)) {
                HashMap<String, Object> map = new HashMap<>();
                map.put(USER_ID_FIELD, user.getUserId());
                return Observable.just(builder()
                        .data(map)
                        .status(SUCCESS)
                        .build());
            } else if (isRegisterByCredential(input)) {
                HashMap<String, Object> map = new HashMap<>();
                map.put(USER_NAME_FIELD, user.getUserName());
                map.put(CODE_FIELD, user.getCode());
                return Observable.just(builder()
                        .data(map)
                        .status(NEED_APPROVAL)
                        .build());
            }
        } else {
            String code = input.getData().get(CODE_FIELD);
            if (code.equals(REG_CODE) && userName.equals(user.getUserName())) {
                HashMap<String, Object> map = new HashMap<>();
                map.put(USER_ID_FIELD, user.getUserId());
                return Observable.just(builder()
                        .redirectUrl("http://relying.party/gateway")
                        .data(map)
                        .status(SUCCESS)
                        .build());
            }
        }
        return Observable.error(new RegistrationException("Registration failed"));
    }

    private boolean isRegisterByLogin(String userName, String password, User user) {
        return userName != null && userName.equals(user.getUserName()) && password != null && password.equals(user.getPassword());
    }

    private boolean isRegisterByCredential(RegInput input) {
        return input.getData().get(CREDENTIAL).equals(CREDENTIAL);
    }

    private boolean isFirstStep(RegInput input) {
        return !input.getData().containsKey(CODE_FIELD);
    }

    @Override
    public boolean isRegCodeSupported() {
        return true;
    }

}

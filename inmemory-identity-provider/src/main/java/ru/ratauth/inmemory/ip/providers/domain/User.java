package ru.ratauth.inmemory.ip.providers.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Value;

import java.util.HashMap;
import java.util.Map;

@Getter
@Builder
public class User {

    private final String userName;
    private final String userId;
    private final String password;
    private final String code;
    private final Map<String, String> optionalData;

    public String getField(String fieldName) {
        if (optionalData == null) {
            return null;
        }
        return optionalData.get(fieldName);
    }

    public User putValue(String key, String value) {
        User newUser = cloneObject().build();
        newUser.optionalData.put(key, value);
        return newUser;
    }

    private User.UserBuilder cloneObject() {
        Map<String, String> map = new HashMap<>();
        if (optionalData != null) {
            map.putAll(optionalData);
        }
        return User.builder()
                .userName(userName)
                .userId(userId)
                .password(password)
                .code(code)
                .optionalData(map);
    }

}

package ru.ratauth.entities;

import java.util.HashMap;
import java.util.Map;

public class UserInfo {

    private final Map<String, Object> userInfo;

    public UserInfo(Map<String, Object> userInfo) {
        this.userInfo = new HashMap<>(userInfo);
    }

    public String getAt(String key) {
        Object o = userInfo.get(key);
        return o != null ? o.toString() : null;
    }

}

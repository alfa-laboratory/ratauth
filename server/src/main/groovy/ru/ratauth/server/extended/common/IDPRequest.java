package ru.ratauth.server.extended.common;

import lombok.Data;
import net.minidev.json.JSONObject;
import ru.ratauth.entities.UserInfo;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static java.util.Optional.ofNullable;

@Data
public class IDPRequest {
    private JSONObject data;
    private JSONObject userInfo;
    private String enroll;
    private String relyingParty;

    public IDPRequest(Map<String, String> data, UserInfo userInfo, String relyingParty, String enroll) {
        this.data = new JSONObject(data);
        this.userInfo = new JSONObject(toMap(userInfo));
        this.enroll = enroll;
        this.relyingParty = relyingParty;
    }

    public String toJsonString() {
        Map<String, Object> result = new HashMap<>();
        result.put("data", data);
        result.put("userInfo", userInfo);
        result.put("relying_party", relyingParty);
        result.put("enroll", enroll);
        return new JSONObject(result).toJSONString();
    }

    private Map<String, Object> toMap(UserInfo userInfo) {
        return ofNullable(userInfo)
                .map(UserInfo::toMap)
                .orElse(Collections.emptyMap());
    }
}

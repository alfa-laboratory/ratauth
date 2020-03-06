package ru.ratauth.providers.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.ratauth.entities.DeviceInfo;
import ru.ratauth.entities.Enroll;
import ru.ratauth.entities.UserInfo;

import java.util.Map;

@Data
@AllArgsConstructor
public class JmsVerifyInput extends VerifyInput {
    private String clientId;
    private DeviceInfo oldDeviceInfo;
    private DeviceInfo deviceInfo;

    public JmsVerifyInput(Map<String, String> data, Enroll enroll, UserInfo userInfo, String relyingParty) {
        super(data, enroll, userInfo, relyingParty);
    }
}

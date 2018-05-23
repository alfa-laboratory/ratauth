package ru.ratauth.server.services;

import ru.ratauth.entities.DeviceInfo;
import rx.Observable;

import java.util.Map;

public interface DeviceService {

    Observable<DeviceInfo> resolveDeviceInfo(String clientId, String authContext, DeviceInfo deviceInfo, Map<String, Object> userInfo);

}

package ru.ratauth.services;

import ru.ratauth.entities.DeviceInfo;
import rx.Observable;

import java.util.Map;

public interface DeviceInfoEventService {

    Observable<DeviceInfo> sendChangeDeviceInfoEvent(String clientId, String enroll, DeviceInfo oldDeviceInfo, DeviceInfo deviceInfo, Map<String, Object> userInfo);

}

package ru.ratauth.services;

import ru.ratauth.entities.DeviceInfo;
import rx.Observable;

public interface DeviceInfoEventService {

    Observable<DeviceInfo> sendChangeDeviceInfoEvent(String clientId, String enroll, DeviceInfo oldDeviceInfo, DeviceInfo deviceInfo);

}

package ru.ratauth.server.services;

import ru.ratauth.entities.DeviceInfo;
import rx.Observable;

import java.util.List;

public interface DeviceService {

    Observable<DeviceInfo> resolveDeviceInfo(String clientId, String enroll, DeviceInfo deviceInfo);

}

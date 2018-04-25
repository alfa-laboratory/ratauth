package ru.ratauth.services;

import ru.ratauth.entities.DeviceInfo;
import rx.Observable;

public interface DeviceInfoService {

    Observable<DeviceInfo> create(String clientId, DeviceInfo deviceInfo);

}

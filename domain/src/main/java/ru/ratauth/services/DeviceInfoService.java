package ru.ratauth.services;

import ru.ratauth.entities.DeviceInfo;
import rx.Observable;

import java.util.List;

public interface DeviceInfoService {

    Observable<DeviceInfo> create(String clientId, String enroll, DeviceInfo deviceInfo);

    Observable<List<DeviceInfo>> findByUserIdAndClientId(String userId, String clientId);

}

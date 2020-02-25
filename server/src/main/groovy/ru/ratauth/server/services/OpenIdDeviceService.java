package ru.ratauth.server.services;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.ratauth.entities.DeviceInfo;
import ru.ratauth.services.DeviceInfoEventService;
import ru.ratauth.services.DeviceInfoService;
import rx.Observable;
import rx.schedulers.Schedulers;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OpenIdDeviceService implements DeviceService {

    private final DeviceInfoService deviceInfoService;
    private final DeviceInfoEventService deviceInfoEventService;

    @Override
    public Observable<DeviceInfo> saveDeviceInfo(String clientId, String enroll, DeviceInfo deviceInfo, Map<String, Object> userInfo) {
        return deviceInfoService.create(clientId, enroll, deviceInfo);
    }

    public Observable<DeviceInfo> sendDeviceInfo(String clientId, String enroll, DeviceInfo deviceInfo, Map<String, Object> userInfo) {
        return deviceInfoService
                .findByUserId(deviceInfo.getUserId())
                .map(oldDevices -> {
                            deviceInfoEventService.sendChangeDeviceInfoEvent(
                                    clientId,
                                    enroll,
                                    getLastDevice(oldDevices).orElseGet(DeviceInfo::new),
                                    deviceInfo,
                                    userInfo
                            ).subscribeOn(Schedulers.io());
                            return deviceInfo;
                        }
                );
    }

    private Optional<DeviceInfo> getLastDevice(List<DeviceInfo> oldDevices) {
        return oldDevices.stream()
                .sorted(Comparator.comparing(DeviceInfo::getCreationDate))
                .skip(1)
                .findFirst();
    }

}

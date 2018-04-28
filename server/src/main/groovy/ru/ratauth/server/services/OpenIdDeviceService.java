package ru.ratauth.server.services;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.ratauth.entities.DeviceInfo;
import ru.ratauth.services.DeviceInfoService;
import rx.Observable;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OpenIdDeviceService implements DeviceService {

    private final DeviceInfoService deviceInfoService;
    private final DeviceInfoEventService deviceInfoEventService;

    @Override
    public Observable<DeviceInfo> resolveDeviceInfo(String clientId, String enroll, DeviceInfo deviceInfo) {

        deviceInfoService.findByUserId(deviceInfo.getUserId())
                .map(oldDevices -> {

                    if (oldDevices.stream().noneMatch(filterDevices(deviceInfo))) {

                        try {
                            deviceInfoEventService.sendChangeDeviceInfoEvent(
                                    clientId,
                                    enroll,
                                    getLastDevice(oldDevices).orElseGet(DeviceInfo::new),
                                    deviceInfo
                            );
                        } catch (Exception e) {
                            log.error("Can't send event message", e);
                        }
                    }
                    return deviceInfo;
                });

        return deviceInfoService.create(clientId, enroll, deviceInfo);
    }

    private Optional<DeviceInfo> getLastDevice(List<DeviceInfo> oldDevices) {
        return oldDevices.stream()
                .max(Comparator.comparing(DeviceInfo::getCreationDate));
    }

    private Predicate<? super DeviceInfo> filterDevices(DeviceInfo newDevice) {
        return oldDevice ->
                isSameDeviceName(oldDevice, newDevice)
                        && isSameDeviceModel(oldDevice, newDevice)
                        && isSameBootTime(oldDevice, newDevice)
                        ||
                        isSameUserAgent(oldDevice, newDevice);
    }

    private boolean isSameUserAgent(DeviceInfo oldDevice, DeviceInfo newDevice) {
        return Objects.equals(oldDevice.getDeviceUserAgent(), newDevice.getDeviceUserAgent());
    }

    private boolean isSameDeviceModel(DeviceInfo oldDevice, DeviceInfo newDevice) {
        return Objects.equals(oldDevice.getDeviceModel(), newDevice.getDeviceModel());
    }

    private boolean isSameBootTime(DeviceInfo oldDevice, DeviceInfo newDevice) {
        return Objects.equals(oldDevice.getDeviceBootTime(), newDevice.getDeviceBootTime());
    }

    private boolean isSameDeviceName(DeviceInfo oldDevice, DeviceInfo newDevice) {
        return (oldDevice.getDeviceName() == null && newDevice.getDeviceName() == null)
                || oldDevice.getDeviceName().equals(newDevice.getDeviceName());
    }

}

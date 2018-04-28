package ru.ratauth.server.services;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.ratauth.entities.DeviceInfo;
import ru.ratauth.services.DeviceInfoService;
import rx.Observable;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OpenIdDeviceService implements DeviceService {

    private final DeviceInfoService deviceInfoService;

    @Override
    public Observable<DeviceInfo> resolveDeviceInfo(String clientId, String enroll, DeviceInfo deviceInfo) {
        return deviceInfoService.create(clientId, enroll, deviceInfo);
    }

}

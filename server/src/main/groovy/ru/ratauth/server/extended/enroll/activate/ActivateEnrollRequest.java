package ru.ratauth.server.extended.enroll.activate;

import lombok.Builder;
import lombok.Data;
import ru.ratauth.entities.AcrValue;
import ru.ratauth.entities.Enroll;

import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.Set;

@Data
@Builder
public class ActivateEnrollRequest {
    private String mfaToken;
    private Map<String, String> data;
    private String clientId;
    private Set<String> scope;
    private AcrValue authContext;
    private Enroll enroll;
    private Instant creationDate;


    private String deviceAppVersion;
    private String deviceId;
    private String deviceUUID;
    private String deviceModel;
    private String deviceGeo;
    private String deviceLocale;
    private String deviceCity;
    private String deviceName;
    private String deviceOSVersion;
    private String deviceBootTime;
    private String deviceTimezone;

    private String deviceIp;
    private String deviceUserAgent;
}

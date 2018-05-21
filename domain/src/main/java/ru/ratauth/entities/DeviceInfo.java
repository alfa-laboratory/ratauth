package ru.ratauth.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceInfo {

    private String id;
    private String userId;

    private String deviceAppVersion;
    private String deviceId;
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

    private Date creationDate = new Date();
}

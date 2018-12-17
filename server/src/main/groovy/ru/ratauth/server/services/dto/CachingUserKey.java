package ru.ratauth.server.services.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CachingUserKey {
    String userId;
    String acrValue;
}

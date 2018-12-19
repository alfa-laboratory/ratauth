package ru.ratauth.server.services.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class CachingUserKey implements Serializable {
    String userId;
    String acrValue;
}

package ru.ratauth.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateDataEntry {

    private String sessionToken;
    private String code;
    private String reason;
    private boolean required;
    private String service;
    private String redirectUri;
    private LocalDateTime created;
    private LocalDateTime expiresAt;
    private LocalDateTime used;
}

package ru.ratauth.entities;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateDataEntry {

    private String sessionToken;
    private String code;
    private String reason;
    private String service;
    private String uri;
    private LocalDateTime created;
    private LocalDateTime expiresAt;
    private LocalDateTime used;
}

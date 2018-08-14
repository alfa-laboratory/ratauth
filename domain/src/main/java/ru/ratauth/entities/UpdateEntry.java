package ru.ratauth.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateEntry {
    private String sessionId;
    private String token;
    private String expiresAt;
    private long used;
}
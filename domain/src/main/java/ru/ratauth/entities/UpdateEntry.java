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
public class UpdateEntry {

    private String sessionId;
    private String token;
    private LocalDateTime expiresAt;
    private LocalDateTime used;
}
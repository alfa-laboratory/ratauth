package ru.ratauth.interaction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InvalidateSessionRequest {

  private String refreshToken;
  private String clientId;
}

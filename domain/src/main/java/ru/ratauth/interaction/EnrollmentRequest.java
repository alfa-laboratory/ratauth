package ru.ratauth.interaction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Set;

/**
 * @author mgorelikov
 * @since 01/02/17
 */
@Builder
@Data
@AllArgsConstructor
public class EnrollmentRequest {
  private String accessToken;
  private Set<String> acrValues;
  private String clientId;
  private String clientPassword;
}

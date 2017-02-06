package ru.ratauth.server.handlers.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * @author mgorelikov
 * @since 01/02/17
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentRequestDTO {
  @JsonProperty("access_token")
  private String accessToken;
  @JsonProperty("acr_values")
  private Set<String> acrValues;
}

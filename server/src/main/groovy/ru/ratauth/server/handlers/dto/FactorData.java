package ru.ratauth.server.handlers.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author mgorelikov
 * @since 02/02/17
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FactorData {
  @JsonProperty("factor_type")
  private String factorType;
  private String provider;
  @JsonProperty("activation_url")
  private String activationURL;
}

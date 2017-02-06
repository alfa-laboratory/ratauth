package ru.ratauth.server.handlers.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author mgorelikov
 * @since 02/02/17
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FactorListDTO {
  @JsonProperty("factors")
  private List<FactorData> factors;
}

package ru.ratauth.server.handlers.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

/**
 * @author mgorelikov
 * @since 02/02/17
 */
public class FactorActivatedDTO {
  @JsonProperty("required_fields")
  private List<Map<String,String>> requiredFields;
}

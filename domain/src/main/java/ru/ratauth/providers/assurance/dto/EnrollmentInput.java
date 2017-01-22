package ru.ratauth.providers.assurance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * @author mgorelikov
 * @since 20/01/17
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EnrollmentInput {
  private String userId;
  // that's right, map of string/object - it was made for abstract input that can be pass through concrete identity provider
  private Map<String,Object> additionalData;
}

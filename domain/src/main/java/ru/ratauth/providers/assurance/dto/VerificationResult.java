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
public class VerificationResult {
  private Map<String,Object> additionalUserInfo;
}

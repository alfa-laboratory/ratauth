package ru.ratauth.providers.registrations.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.ratauth.providers.assurance.dto.AssuranceStatus;

import java.util.Map;

/**
 * @author mgorelikov
 * @since 28/01/16
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegResult {
  private Status status;
  private String userId;
  private Map<String, Object> userInfo;
  private String assuranceLevel;

  public enum Status {
    SUCCESS,
    NEED_APPROVAL
  }
}

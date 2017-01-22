package ru.ratauth.providers.registrations.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.ratauth.providers.assurance.dto.AssuranceStatus;

import java.util.Map;

/**
 * @author mgorelikov
 * @since 22/01/17
 */
@Data
@NoArgsConstructor
public class AssuredRegResult extends RegResult {

  private Map<String,Object> assuranceData;
  private AssuranceStatus assuranceStatus;

  @Builder
  public AssuredRegResult(Status status, String userId, Map<String, Object> userInfo,
                          Map<String,Object> assuranceData, AssuranceStatus assuranceStatus) {
    super(status, userId, userInfo);
    this.assuranceData = assuranceData;
    this.assuranceStatus = assuranceStatus;
  }
}

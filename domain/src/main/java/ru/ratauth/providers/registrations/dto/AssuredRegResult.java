package ru.ratauth.providers.registrations.dto;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import ru.ratauth.entities.assurance.FactorProviderData;
import ru.ratauth.providers.assurance.dto.AssuranceStatus;

import java.util.Map;

/**
 * @author mgorelikov
 * @since 22/01/17
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AssuredRegResult extends RegResult {

  private Map<String,FactorProviderData> providerData;
  private AssuranceStatus assuranceStatus;

  @Builder
  public AssuredRegResult(Status status, String userId, Map<String, Object> userInfo,
                          Map<String,FactorProviderData> providerData, AssuranceStatus assuranceStatus, String assuranceLevel) {
    super(status, userId, userInfo, assuranceLevel);
    this.providerData = providerData;
    this.assuranceStatus = assuranceStatus;
  }

  public static class AssuredRegResultBuilder extends RegResultBuilder {
    public AssuredRegResultBuilder() {
      super();
    }
  }
}

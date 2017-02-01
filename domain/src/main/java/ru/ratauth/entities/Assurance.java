package ru.ratauth.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.ratauth.providers.assurance.dto.AssuranceStatus;

import java.util.Date;
import java.util.Map;
import java.util.Set;

/**
 * @author mgorelikov
 * @since 23/01/17
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Assurance {

  private String id;
  private String sessionId;
  private Date created;
  /**
   * unique name
   */
  private String relyingParty;
  private String redirectURL;

  private String identityProvider;

  private Set<String> acrValues;
  private String acr;
  private AssuranceStatus status;

  private Map<String,Object> additionalUserInfo;

  private Map<String,Object> enrollmentInfo;

  private Date lastUpdate;
}

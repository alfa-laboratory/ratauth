package ru.ratauth.entities;

import lombok.*;

import java.util.Set;

/**
 * @author mgorelikov
 * @since 01/11/15
 */

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class RelyingParty extends AuthClient {
  private String redirectURL;
  private ApplicationType applicationType;
  /**
   * unique name
   */
  private String identityProvider;
  private Long codeTTL;
  private Long tokenTTL;
  private Long refreshTokenTTL;
  private Long sessionTTL;
  /**
   * Set of unique grants (means only internal grants, it's not resource server scope)
   */
  private Set<String> grants;
}

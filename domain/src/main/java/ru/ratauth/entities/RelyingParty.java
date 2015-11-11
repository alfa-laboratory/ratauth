package ru.ratauth.entities;

import lombok.*;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * @author mgorelikov
 * @since 01/11/15
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RelyingParty {
  private String id;
  private String name;//description name
  private String password;
  private String secret;
  private Date created;
  private Date updated;
  private @Singular("baseAddress") Set<String> baseAddress;//optional field for relying party interconnection
  private Integer secretTTL;
  private String redirectURL;
  private ApplicationType applicationType;
  private String identityProvider;//identifier
  private @Singular Set<String> resourceServers;
  private ServiceStatus status;
}

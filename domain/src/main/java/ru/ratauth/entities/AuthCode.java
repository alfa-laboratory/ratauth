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
public class AuthCode {
  private String id;
  private String code;
  private Date created;
  private Date used;
  private Long TTL;
  private @Singular("scope")
  Set<String> scopes;
  private String relyingParty;//identifier
  private String identityProvider;//identifier
  private AuthCodeStatus status;

  public Long expiresIn() {
    return created.getTime() + TTL;
  }
}

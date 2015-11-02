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
public class Token {
  private String token;
  private Date created;
  private String codeId;//identifier
  private Long TTL;
  private @Singular("scope") Set<String> scopes;
  private String relyingParty;//identifier
  private String resourceServer;//identifier
  private String identityProvider;//identifier
  private String user;//external identifier

  public Long expiresIn() {
    return created.getTime() + TTL;
  }
}

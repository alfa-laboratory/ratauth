package ru.ratauth.entities;

import lombok.*;

import java.util.Date;
import java.util.Optional;
import java.util.Set;

/**
 * @author mgorelikov
 * @since 16/02/16
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Session {
  /**
   * unique primary key
   */
  private String id;

  /**
   * unique name
   */
  private String identityProvider;

  /**
   * JWT signed by server secret
   */
  private String userInfo;
  private Status status;
  private Date created;
  private Date blocked;
  private Date expiresIn;
  /**
   * for optional process that checks user status in background
   */
  private Date lastCheck;
  private Set<AuthEntry> entries;

  public Optional<AuthEntry> getEntry(String relyingPartyId) {
    if(entries == null)
      return Optional.empty();
    return entries.stream().filter(it -> it.getRelyingParty().equals(relyingPartyId)).findFirst();
  }
  public Optional<Token> getToken(String relyingPartyId) {
    return this.getEntry(relyingPartyId).flatMap(el -> el.getLatestToken());
  }
}

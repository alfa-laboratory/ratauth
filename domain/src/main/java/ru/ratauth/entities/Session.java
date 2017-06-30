package ru.ratauth.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
import java.util.Date;
import java.util.Optional;
import java.util.Set;

/**
 * @author mgorelikov
 * @since 16/02/16
 */
@Data
@Slf4j
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
   * unique name of primary relying party for this session
   */
  private String authClient;


  private String sessionToken;

  private String mfaToken;

  /**
   * Abstract user identifier got from provider
   */
  private String userId;

  /**
   * JWT with user info signed by server secret
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
    return entries.stream()
        .filter(it -> it.getRelyingParty().equals(relyingPartyId))
        .sorted(Comparator.comparing(AuthEntry::getCodeExpiresIn).reversed())
        .findFirst();
  }
  public Optional<Token> getToken(String relyingPartyId) {
    return this.getEntry(relyingPartyId).flatMap(AuthEntry::getLatestToken);
  }

  public AuthEntry getPrimaryEntry() {
    if(entries == null)
      throw new InternalError("Session doesn't have primary entry");
    return entries.stream().filter(it -> AuthType.COMMON == it.getAuthType()).findFirst()
      .orElseThrow(() -> new InternalError("Session doesn't have primary entry"));
  }
}

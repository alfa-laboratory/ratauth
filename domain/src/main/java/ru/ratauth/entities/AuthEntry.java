package ru.ratauth.entities;

import lombok.*;

import java.util.Date;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * @author mgorelikov
 * @since 01/11/15
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthEntry {
  private String id;
  private String authCode;
  private Date codeExpiresIn;
  private Date created;
  private String refreshToken;
  private Date refreshTokenExpiresIn;
  @Singular
  private Set<String> scopes;
  /**
   * unique name
   */
  private String relyingParty;
  @Singular
  private Set<Token> tokens;
  private AuthType authType;
  private String redirectUrl;

  public void addToken(Token token) {
    if (this.tokens == null || this.tokens.isEmpty())
      this.tokens = new HashSet<>();
    this.tokens.add(token);
  }

  public Optional<Token> getLatestToken() {
    if (tokens == null)
      return Optional.empty();
    return tokens.stream()
        .filter(el -> el != null)
        .sorted((el1, el2) -> el1.getCreated().compareTo(el2.getCreated()))
        .findFirst();
  }
}

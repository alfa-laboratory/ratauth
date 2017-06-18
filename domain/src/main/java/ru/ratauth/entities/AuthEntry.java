package ru.ratauth.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

import static java.util.Objects.requireNonNull;

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
  private Set<String> scopes;
  private Set<String> authContext;
  /**
   * unique name
   */
  private String relyingParty;
  private Set<Token> tokens;
  private AuthType authType;
  private String redirectUrl;

  public void mergeAuthContext(Set<String> authContext) {
    this.authContext.addAll(requireNonNull(authContext));
  }

  public void addToken(Token token) {
    if(token == null)
      return;
    if (this.tokens == null || this.tokens.isEmpty())
      this.tokens = new HashSet<>();
    this.tokens.add(token);
  }

  public Optional<Token> getLatestToken() {
    if (tokens == null)
      return Optional.empty();
    return tokens.stream()
        .filter(el -> el != null)
        .sorted((el1, el2) -> el2.getExpiresIn().compareTo(el1.getExpiresIn()))
        .findFirst();
  }
}

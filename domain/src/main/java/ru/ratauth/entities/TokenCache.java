package ru.ratauth.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author mgorelikov
 * @since 16/02/16
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TokenCache {
  /**
   * access token
   */
  private String token;
  /**
   * JWT token signed by client secret
   */
  private String idToken;
  /**
   * Session identifier
   */
  private String session;

  private Date created;

  /**
   * unique name
   */
  private String client;
}

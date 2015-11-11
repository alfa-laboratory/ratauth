package ru.ratauth.entities;

import lombok.*;

import java.util.Date;
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
  private Long TTL;
  private String idToken;//optional jwt

  public Long expiresIn() {
    return created.getTime() + TTL;
  }
}

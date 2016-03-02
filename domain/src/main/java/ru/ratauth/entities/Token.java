package ru.ratauth.entities;

import lombok.*;

import java.util.Date;

/**
 * @author mgorelikov
 * @since 01/11/15
 *
 * Entity for access token
 */
@Builder
@Data
@EqualsAndHashCode(exclude = {"created", "expiresIn"})
@AllArgsConstructor
@NoArgsConstructor
public class Token {
  /**
   * unique
   */
  private String token;
  private Date created;
  private Date expiresIn;
}

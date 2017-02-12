package ru.ratauth.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @author mgorelikov
 * @since 12/02/2017
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class SessionClient extends AuthClient {
  private Long sessionTokenTTL;
}

package ru.ratauth.server.database.entities;

import groovy.transform.builder.Builder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

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
  private Integer TTL;
  private String resourceConsumer;//identifier
  private String resourceProvider;//identifier
  private String authProvider;//identifier
  private String user;//external identifier
  private AuthCodeStatus status;
}

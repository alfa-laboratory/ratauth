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
public class Token {
  private String token;
  private Date created;
  private String codeId;//identifier
  private Integer TTL;
}

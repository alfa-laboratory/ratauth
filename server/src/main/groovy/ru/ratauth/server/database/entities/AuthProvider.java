package ru.ratauth.server.database.entities;

import groovy.transform.builder.Builder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author mgorelikov
 * @since 02/11/15
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthProvider {
  private String id;
  private String name;
  private String type;
}

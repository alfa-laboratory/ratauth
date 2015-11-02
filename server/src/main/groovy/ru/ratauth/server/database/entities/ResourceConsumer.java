package ru.ratauth.server.database.entities;

import groovy.transform.builder.Builder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * @author mgorelikov
 * @since 01/11/15
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResourceConsumer {
  private String id;
  private String login;
  private String password;
  private String name;
  private Map<String, List<AuthProvider>> providersLink;//map with resource provider identifier as key and list of auth providers as value
}

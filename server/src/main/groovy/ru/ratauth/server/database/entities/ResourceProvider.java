package ru.ratauth.server.database.entities;

import groovy.transform.builder.Builder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author mgorelikov
 * @since 01/11/15
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResourceProvider {
  private String id;
  private String name;
  private List<AuthProvider> availableProviders;//available auth providers for the resource provider
}

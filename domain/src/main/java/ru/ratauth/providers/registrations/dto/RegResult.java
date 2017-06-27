package ru.ratauth.providers.registrations.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.Set;

/**
 * @author mgorelikov
 * @since 28/01/16
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegResult {
  private Status status;
  private String redirectUrl;
  private Map<String, Object> data;//login
  private Set<String> authContext;

  public enum Status {
    SUCCESS,
    NEED_APPROVAL
  }
}

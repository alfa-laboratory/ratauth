package ru.ratauth.interaction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author mgorelikov
 * @since 29/01/16
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegistrationResponse {
  private String userId;
}

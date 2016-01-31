package ru.ratauth.providers.auth.dto;

import lombok.*;

import java.util.Map;

/**
 * @author mgorelikov
 * @since 26/01/16
 * * Still different provider could produce different fields during auth/registration process and could have multiphase flow - so we have abstract map of objects
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResult {
  //some abstract data map that could contain any registration data(login, personal code etc.)
  @Singular("field")
  private Map<String, Object> data;
}

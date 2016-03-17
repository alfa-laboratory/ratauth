package ru.ratauth.server.handlers.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.ratauth.providers.registrations.dto.RegResult;

import java.util.Map;

/**
 * @author mgorelikov
 * @since 02/02/16
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RegisterDTO {
  private String status;
  private Map<String, Object> data;
  private String code;

  public RegisterDTO(RegResult regResult) {
    this.status = regResult.getStatus().toString();
    this.data = regResult.getData();
  }
}

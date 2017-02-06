package ru.ratauth.server.handlers.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.ratauth.providers.assurance.dto.AssuranceStatus;

/**
 * @author mgorelikov
 * @since 02/02/17
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EnrollmentDTO {
  @JsonProperty("enrollment_id")
  private String enrollmentId;
  private AssuranceStatus status;
  @JsonProperty("enrollment_url")
  private String enrollmentURL;
}

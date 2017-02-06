package ru.ratauth.entities.assurance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author mgorelikov
 * @since 06/02/17
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FactorField {
  private String name;
  private FieldType type;
  private Integer length;
}

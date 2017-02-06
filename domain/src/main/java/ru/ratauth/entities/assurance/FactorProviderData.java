package ru.ratauth.entities.assurance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * @author mgorelikov
 * @since 06/02/17
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FactorProviderData {
  private Map<String, String> additionaFactorlInfo;
  private List<FactorField> requiredFields;
  private List<String> factorTypes;
}

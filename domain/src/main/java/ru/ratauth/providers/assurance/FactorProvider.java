package ru.ratauth.providers.assurance;

import ru.ratauth.providers.assurance.dto.*;
import rx.Observable;

/**
 * Interface of providers that could increase nist_auth level of existing session
 * according to http://nvlpubs.nist.gov/nistpubs/Legacy/SP/nistspecialpublication800-63ver1.0.2.pdf
 * @author mgorelikov
 * @since 19/01/17
 */
public interface FactorProvider {
  /**
   * Method that returns Assurance Level (acr value)
   * that provided by this provider
   */
  String getProvidedAssuranceLevel();

  /**
   * Method that initiate assurance level increase
   * Must do any preparation processes
   * @param input
   * @return
   */
  Observable<EnrollmentResult> enroll(EnrollmentInput input);

  /**
   * Method that activate assurance level increase process
   * @param input
   * @return
   */
  Observable<ActivationResult> activate(ActivationInput input);

  /**
   * Method that verify code - final step of enrollment process
   * @param input
   * @return
   */
  Observable<VerificationResult> verify(VerificationInput input);
}

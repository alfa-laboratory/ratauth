package ru.ratauth.server.services;

import ru.ratauth.interaction.RegistrationRequest;
import ru.ratauth.interaction.TokenResponse;
import ru.ratauth.providers.registrations.dto.RegResult;
import rx.Observable;

/**
 * @author mgorelikov
 * @since 29/01/16
 */
public interface RegistrationService {
  /**
   * Initial step of registration. Registration could be one or two-step, depends on registration provider
   * @param request registration request
   * @return registration result. Contains status and addition data from provider
   */
  Observable<RegResult> register(RegistrationRequest request);

  /**
   * Second step of two-step registration process. It will be called in case of NEED_APPROVAL status on firs step.
   * @param request registration approval request
   * @return registration result. Contains status and addition data from provider
   */
  Observable<TokenResponse> finishRegister(RegistrationRequest request);
}

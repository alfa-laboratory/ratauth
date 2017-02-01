package ru.ratauth.services;

import ru.ratauth.entities.Assurance;
import rx.Observable;

/**
 * @author mgorelikov
 * @since 31/01/17
 *
 * Persistence layer
 * Async assurance service
 */
public interface AssuranceService {

  /**
   * Saves Assurance entity into persistence layer
   * @param assurance
   * @return
   */
  Observable<Assurance> create(Assurance assurance);

}

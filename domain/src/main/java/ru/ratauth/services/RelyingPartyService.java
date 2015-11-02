package ru.ratauth.services;

import ru.ratauth.entities.RelyingParty;

/**
 * @author mgorelikov
 * @since 02/11/15
 */
public interface RelyingPartyService {
  RelyingParty getRelyingParty(String id);
}

package ru.ratauth.entities

import ru.ratauth.server.utils.DateUtils
import spock.lang.Specification

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

/**
 * @author tolkv
 * @version 19/04/2017
 */
class SessionTest extends Specification {
  def 'should return newest session'() {
    given:
    final LocalDateTime now = LocalDateTime.now();
    final LocalDateTime authCodeExpires = now.plus(100, ChronoUnit.SECONDS)
    final LocalDateTime authCodeExpiresLatest = now.plus(300, ChronoUnit.SECONDS)
    def session = Session.builder()
        .entries([
        [
            relyingParty :'rp1',
            authCode     :'11111',
            created      :DateUtils.fromLocal(now),
            codeExpiresIn:DateUtils.fromLocal(authCodeExpires),
        ] as AuthEntry,
        [
            relyingParty :'rp1',
            authCode     :'2222',
            created      :DateUtils.fromLocal(now.plusDays(1)),
            codeExpiresIn:DateUtils.fromLocal(authCodeExpiresLatest),
        ] as AuthEntry
    ] as Set<AuthEntry>)
        .build()

    when:
    Optional<AuthEntry> entry = session.getEntry('rp1')

    then:
    entry.ifPresent {
      assert it.authCode == '2222'
    }
  }
}

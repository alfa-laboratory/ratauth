package ru.ratauth.services;

import java.time.LocalDateTime;
import ru.ratauth.entities.UpdateEntry;
import rx.Observable;

public interface UpdateTokenService {

    /**
     * Create entry with auto generated update_token = random.uuid
     * @param sessionId
     * @param expiresAt
     * @return
     */
    Observable<UpdateEntry> createEntry(String sessionId, LocalDateTime expiresAt);

    /**
     * Method return true if token exists and is not expired
     * @param token
     * @return
     */
    Observable<Boolean> isValidToken(String token);

    /**
     * Invalidate update token
     * @param token update_token parameter
     * @return "true" if request complete
     */
    Observable<Boolean> invalidateToken(String token);
}
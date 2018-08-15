package ru.ratauth.services;

import java.time.LocalDateTime;
import ru.ratauth.entities.UpdateEntry;
import rx.Observable;

public interface UpdateCodeService {

    /**
     * Create entry with auto generated update_code = random.uuid
     * @param sessionId
     * @param expiresAt
     * @return
     */
    Observable<UpdateEntry> create(String sessionId, LocalDateTime expiresAt);

    /**
     * Method return entry if token exists and is not expired
     * @param code
     * @return
     */
    Observable<UpdateEntry> getValidEntry(String code);

    /**
     * Invalidate update token
     * @param code update_token parameter
     * @return "true" if request complete
     */
    Observable<Boolean> invalidateToken(String code);
}
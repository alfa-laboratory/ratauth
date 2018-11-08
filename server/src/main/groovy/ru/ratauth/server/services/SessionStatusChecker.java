package ru.ratauth.server.services;

import ru.ratauth.entities.Session;

/**
 * @author mgorelikov
 * @since 20/03/16
 */
public interface SessionStatusChecker {
    /**
     * Check user status in authProvider attached to session
     *
     * @param session session for invalidation
     */
    void checkAndUpdateSession(Session session);
}

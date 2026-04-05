package ru.splitus.admin;

import java.util.Optional;

/**
 * Defines persistence operations for admin user.
 */
public interface AdminUserRepository {

    /**
     * Finds by login.
     */
    Optional<AdminUser> findByLogin(String login);

    /**
     * Updates bootstrap user.
     */
    void upsertBootstrapUser(String login, String passwordHash);
}

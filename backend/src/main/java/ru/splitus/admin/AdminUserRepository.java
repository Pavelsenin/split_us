package ru.splitus.admin;

import java.util.Optional;

/**
 * Defines persistence operations for admin users.
 */
public interface AdminUserRepository {

    /**
     * Finds an admin user by login.
     */
    Optional<AdminUser> findByLogin(String login);

    /**
     * Saves a new admin user.
     */
    AdminUser save(AdminUser adminUser);

    /**
     * Updates the password hash for an existing admin user.
     */
    AdminUser updatePasswordHash(AdminUser adminUser, String passwordHash);
}

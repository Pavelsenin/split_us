package ru.splitus.admin;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Represents an admin user allowed to access the admin panel.
 */
public class AdminUser {

    private final UUID id;
    private final String login;
    private final String passwordHash;
    private final OffsetDateTime createdAt;
    private final OffsetDateTime updatedAt;

    /**
     * Creates a new admin user instance.
     */
    public AdminUser(UUID id, String login, String passwordHash, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.id = id;
        this.login = login;
        this.passwordHash = passwordHash;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * Returns the admin user id.
     */
    public UUID getId() {
        return id;
    }

    /**
     * Returns the login.
     */
    public String getLogin() {
        return login;
    }

    /**
     * Returns the password hash.
     */
    public String getPasswordHash() {
        return passwordHash;
    }

    /**
     * Returns the creation timestamp.
     */
    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Returns the update timestamp.
     */
    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}

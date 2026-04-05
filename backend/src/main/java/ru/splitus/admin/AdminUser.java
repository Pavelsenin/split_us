package ru.splitus.admin;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Represents admin user.
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
     * Returns the id.
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
     * Returns the created at.
     */
    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Returns the updated at.
     */
    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}

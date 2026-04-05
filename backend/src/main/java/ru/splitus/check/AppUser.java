package ru.splitus.check;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Represents app user.
 */
public class AppUser {

    private final UUID id;
    private final long telegramUserId;
    private final String telegramUsername;
    private final OffsetDateTime registeredAt;
    private final OffsetDateTime updatedAt;

    /**
     * Creates a new app user instance.
     */
    public AppUser(UUID id, long telegramUserId, String telegramUsername, OffsetDateTime registeredAt, OffsetDateTime updatedAt) {
        this.id = id;
        this.telegramUserId = telegramUserId;
        this.telegramUsername = telegramUsername;
        this.registeredAt = registeredAt;
        this.updatedAt = updatedAt;
    }

    /**
     * Returns the id.
     */
    public UUID getId() {
        return id;
    }

    /**
     * Returns the telegram user id.
     */
    public long getTelegramUserId() {
        return telegramUserId;
    }

    /**
     * Returns the telegram username.
     */
    public String getTelegramUsername() {
        return telegramUsername;
    }

    /**
     * Returns the registered at.
     */
    public OffsetDateTime getRegisteredAt() {
        return registeredAt;
    }

    /**
     * Returns the updated at.
     */
    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}





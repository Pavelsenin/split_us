package ru.splitus.check;

import java.time.OffsetDateTime;
import java.util.UUID;

public class AppUser {

    private final UUID id;
    private final long telegramUserId;
    private final String telegramUsername;
    private final OffsetDateTime registeredAt;
    private final OffsetDateTime updatedAt;

    public AppUser(UUID id, long telegramUserId, String telegramUsername, OffsetDateTime registeredAt, OffsetDateTime updatedAt) {
        this.id = id;
        this.telegramUserId = telegramUserId;
        this.telegramUsername = telegramUsername;
        this.registeredAt = registeredAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public long getTelegramUserId() {
        return telegramUserId;
    }

    public String getTelegramUsername() {
        return telegramUsername;
    }

    public OffsetDateTime getRegisteredAt() {
        return registeredAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}


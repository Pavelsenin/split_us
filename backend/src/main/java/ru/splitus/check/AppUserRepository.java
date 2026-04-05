package ru.splitus.check;

import java.util.Optional;

/**
 * Defines persistence operations for app user.
 */
public interface AppUserRepository {

    /**
     * Finds by telegram user id.
     */
    Optional<AppUser> findByTelegramUserId(long telegramUserId);

    /**
     * Executes save.
     */
    AppUser save(AppUser user);

    /**
     * Updates username.
     */
    AppUser updateUsername(AppUser user, String telegramUsername);
}





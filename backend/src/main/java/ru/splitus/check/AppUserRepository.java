package ru.splitus.check;

import java.util.Optional;

public interface AppUserRepository {

    Optional<AppUser> findByTelegramUserId(long telegramUserId);

    AppUser save(AppUser user);

    AppUser updateUsername(AppUser user, String telegramUsername);
}


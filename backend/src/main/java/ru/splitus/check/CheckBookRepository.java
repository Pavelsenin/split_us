package ru.splitus.check;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

public interface CheckBookRepository {

    CheckBook save(CheckBook checkBook);

    Optional<CheckBook> findById(UUID checkId);

    Optional<CheckBook> findByInviteToken(String inviteToken);

    int countCreatedByOwnerSince(UUID ownerUserId, OffsetDateTime since);
}

package ru.splitus.check;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Defines persistence operations for check book.
 */
public interface CheckBookRepository {

    /**
     * Executes save.
     */
    CheckBook save(CheckBook checkBook);

    /**
     * Finds by id.
     */
    Optional<CheckBook> findById(UUID checkId);

    /**
     * Finds by invite token.
     */
    Optional<CheckBook> findByInviteToken(String inviteToken);

    /**
     * Counts created by owner since.
     */
    int countCreatedByOwnerSince(UUID ownerUserId, OffsetDateTime since);
}




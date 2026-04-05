package ru.splitus.admin;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.splitus.check.CheckBook;
import ru.splitus.check.CheckBookRepository;
import ru.splitus.error.ApiException;

/**
 * Tests admin check command service.
 */
class AdminCheckCommandServiceTest {

    @Test
    void deletesCheckWhenConfirmationMatches() {
        InMemoryCheckBookRepository repository = new InMemoryCheckBookRepository("Trip");
        AdminCheckCommandService service = new AdminCheckCommandService(repository);

        String title = service.deleteCheck(repository.checkId, "Trip");

        Assertions.assertEquals("Trip", title);
        Assertions.assertTrue(repository.deleted);
    }

    @Test
    void rejectsDeleteWhenConfirmationDoesNotMatch() {
        InMemoryCheckBookRepository repository = new InMemoryCheckBookRepository("Trip");
        AdminCheckCommandService service = new AdminCheckCommandService(repository);

        ApiException exception = Assertions.assertThrows(
                ApiException.class,
                () -> service.deleteCheck(repository.checkId, "Other")
        );

        Assertions.assertEquals("Confirmation title does not match the check title", exception.getMessage());
        Assertions.assertFalse(repository.deleted);
    }

    /**
     * In-memory check book repository.
     */
    private static class InMemoryCheckBookRepository implements CheckBookRepository {
        private final UUID checkId = UUID.randomUUID();
        private final CheckBook checkBook;
        private boolean deleted;

        /**
         * Creates a new in-memory repository instance.
         */
        InMemoryCheckBookRepository(String title) {
            this.checkBook = new CheckBook(
                    checkId,
                    title,
                    UUID.randomUUID(),
                    "invite-token",
                    null,
                    "RUB",
                    true,
                    OffsetDateTime.parse("2026-04-05T11:00:00+03:00")
            );
        }

        /**
         * Executes save.
         */
        @Override
        public CheckBook save(CheckBook checkBook) {
            return checkBook;
        }

        /**
         * Finds by id.
         */
        @Override
        public Optional<CheckBook> findById(UUID checkId) {
            return deleted || !this.checkId.equals(checkId) ? Optional.<CheckBook>empty() : Optional.of(checkBook);
        }

        /**
         * Finds by invite token.
         */
        @Override
        public Optional<CheckBook> findByInviteToken(String inviteToken) {
            return Optional.empty();
        }

        /**
         * Counts created by owner since.
         */
        @Override
        public int countCreatedByOwnerSince(UUID ownerUserId, OffsetDateTime since) {
            return 0;
        }

        /**
         * Deletes a check by id.
         */
        @Override
        public boolean deleteById(UUID checkId) {
            if (deleted || !this.checkId.equals(checkId)) {
                return false;
            }
            deleted = true;
            return true;
        }
    }
}

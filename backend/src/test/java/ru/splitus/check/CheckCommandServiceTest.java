package ru.splitus.check;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.splitus.error.ApiErrorCode;
import ru.splitus.error.ApiException;

class CheckCommandServiceTest {

    @Test
    void createCheckCreatesOwnerParticipant() {
        InMemoryAppUserRepository userRepository = new InMemoryAppUserRepository();
        InMemoryCheckBookRepository checkRepository = new InMemoryCheckBookRepository();
        InMemoryParticipantRepository participantRepository = new InMemoryParticipantRepository();
        CheckCommandService service = new CheckCommandService(userRepository, checkRepository, participantRepository);

        CheckSnapshot snapshot = service.createCheck("Weekend", 1001L, "@alice");

        Assertions.assertEquals("Weekend", snapshot.getCheckBook().getTitle());
        Assertions.assertEquals("RUB", snapshot.getCheckBook().getCurrencyCode());
        Assertions.assertEquals(1, snapshot.getParticipants().size());
        Assertions.assertEquals("alice", snapshot.getParticipants().get(0).getDisplayName());
        Assertions.assertEquals(ParticipantType.REGISTERED, snapshot.getParticipants().get(0).getType());
    }

    @Test
    void addGuestParticipantRejectsDuplicateName() {
        InMemoryAppUserRepository userRepository = new InMemoryAppUserRepository();
        InMemoryCheckBookRepository checkRepository = new InMemoryCheckBookRepository();
        InMemoryParticipantRepository participantRepository = new InMemoryParticipantRepository();
        CheckCommandService service = new CheckCommandService(userRepository, checkRepository, participantRepository);

        CheckSnapshot snapshot = service.createCheck("Trip", 1001L, "alice");
        service.addGuestParticipant(snapshot.getCheckBook().getId(), "Bob");

        ApiException exception = Assertions.assertThrows(ApiException.class, () ->
                service.addGuestParticipant(snapshot.getCheckBook().getId(), "Bob")
        );

        Assertions.assertEquals(ApiErrorCode.PARTICIPANT_NAME_CONFLICT, exception.getCode());
    }

    @Test
    void addRegisteredParticipantRejectsDuplicateUser() {
        InMemoryAppUserRepository userRepository = new InMemoryAppUserRepository();
        InMemoryCheckBookRepository checkRepository = new InMemoryCheckBookRepository();
        InMemoryParticipantRepository participantRepository = new InMemoryParticipantRepository();
        CheckCommandService service = new CheckCommandService(userRepository, checkRepository, participantRepository);

        CheckSnapshot snapshot = service.createCheck("Trip", 1001L, "alice");
        service.addRegisteredParticipant(snapshot.getCheckBook().getId(), 1002L, "bob");

        ApiException exception = Assertions.assertThrows(ApiException.class, () ->
                service.addRegisteredParticipant(snapshot.getCheckBook().getId(), 1002L, "bob")
        );

        Assertions.assertEquals(ApiErrorCode.REGISTERED_PARTICIPANT_ALREADY_EXISTS, exception.getCode());
    }

    @Test
    void createCheckRejectsLimitOver100PerDay() {
        InMemoryAppUserRepository userRepository = new InMemoryAppUserRepository();
        InMemoryCheckBookRepository checkRepository = new InMemoryCheckBookRepository();
        InMemoryParticipantRepository participantRepository = new InMemoryParticipantRepository();
        CheckCommandService service = new CheckCommandService(userRepository, checkRepository, participantRepository);

        for (int i = 0; i < 100; i++) {
            service.createCheck("Trip " + i, 1001L, "alice");
        }

        ApiException exception = Assertions.assertThrows(ApiException.class, () ->
                service.createCheck("Trip overflow", 1001L, "alice")
        );

        Assertions.assertEquals(ApiErrorCode.CHECK_CREATION_LIMIT_REACHED, exception.getCode());
    }

    @Test
    void addParticipantRejectsMissingUsername() {
        InMemoryAppUserRepository userRepository = new InMemoryAppUserRepository();
        InMemoryCheckBookRepository checkRepository = new InMemoryCheckBookRepository();
        InMemoryParticipantRepository participantRepository = new InMemoryParticipantRepository();
        CheckCommandService service = new CheckCommandService(userRepository, checkRepository, participantRepository);

        CheckSnapshot snapshot = service.createCheck("Trip", 1001L, "alice");

        ApiException exception = Assertions.assertThrows(ApiException.class, () ->
                service.addRegisteredParticipant(snapshot.getCheckBook().getId(), 1002L, "   ")
        );

        Assertions.assertEquals(ApiErrorCode.VALIDATION_ERROR, exception.getCode());
    }

    private static class InMemoryAppUserRepository implements AppUserRepository {
        private final Map<Long, AppUser> byTelegramId = new HashMap<Long, AppUser>();

        @Override
        public Optional<AppUser> findByTelegramUserId(long telegramUserId) {
            return Optional.ofNullable(byTelegramId.get(Long.valueOf(telegramUserId)));
        }

        @Override
        public AppUser save(AppUser user) {
            byTelegramId.put(Long.valueOf(user.getTelegramUserId()), user);
            return user;
        }

        @Override
        public AppUser updateUsername(AppUser user, String telegramUsername) {
            AppUser updated = new AppUser(user.getId(), user.getTelegramUserId(), telegramUsername, user.getRegisteredAt(), OffsetDateTime.now());
            byTelegramId.put(Long.valueOf(updated.getTelegramUserId()), updated);
            return updated;
        }
    }

    private static class InMemoryCheckBookRepository implements CheckBookRepository {
        private final Map<UUID, CheckBook> checks = new HashMap<UUID, CheckBook>();

        @Override
        public CheckBook save(CheckBook checkBook) {
            checks.put(checkBook.getId(), checkBook);
            return checkBook;
        }

        @Override
        public Optional<CheckBook> findById(UUID checkId) {
            return Optional.ofNullable(checks.get(checkId));
        }

        @Override
        public int countCreatedByOwnerSince(UUID ownerUserId, OffsetDateTime since) {
            int count = 0;
            for (CheckBook checkBook : checks.values()) {
                if (checkBook.getOwnerUserId().equals(ownerUserId) && !checkBook.getCreatedAt().isBefore(since)) {
                    count++;
                }
            }
            return count;
        }
    }

    private static class InMemoryParticipantRepository implements ParticipantRepository {
        private final List<Participant> participants = new ArrayList<Participant>();

        @Override
        public Participant save(Participant participant) {
            participants.add(participant);
            return participant;
        }

        @Override
        public int countByCheckId(UUID checkId) {
            int count = 0;
            for (Participant participant : participants) {
                if (participant.getCheckId().equals(checkId) && participant.isActive()) {
                    count++;
                }
            }
            return count;
        }

        @Override
        public boolean existsByCheckIdAndDisplayName(UUID checkId, String displayName) {
            for (Participant participant : participants) {
                if (participant.getCheckId().equals(checkId) && participant.isActive() && participant.getDisplayName().equals(displayName)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public Optional<Participant> findActiveRegisteredParticipant(UUID checkId, UUID userId) {
            for (Participant participant : participants) {
                if (participant.getCheckId().equals(checkId)
                        && participant.isActive()
                        && userId.equals(participant.getLinkedUserId())) {
                    return Optional.of(participant);
                }
            }
            return Optional.empty();
        }

        @Override
        public List<Participant> findByCheckId(UUID checkId) {
            List<Participant> result = new ArrayList<Participant>();
            for (Participant participant : participants) {
                if (participant.getCheckId().equals(checkId)) {
                    result.add(participant);
                }
            }
            result.sort(Comparator.comparing(Participant::getCreatedAt));
            return result;
        }
    }
}


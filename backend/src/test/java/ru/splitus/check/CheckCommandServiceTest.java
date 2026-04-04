package ru.splitus.check;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.splitus.error.ApiErrorCode;
import ru.splitus.error.ApiException;
import ru.splitus.expense.Expense;
import ru.splitus.expense.ExpenseRepository;
import ru.splitus.expense.ExpenseShare;
import ru.splitus.expense.ExpenseShareRepository;
import ru.splitus.expense.ExpenseStatus;

class CheckCommandServiceTest {

    @Test
    void createCheckCreatesOwnerParticipant() {
        TestContext context = new TestContext();
        CheckCommandService service = context.service;

        CheckSnapshot snapshot = service.createCheck("Weekend", 1001L, "@alice");

        Assertions.assertEquals("Weekend", snapshot.getCheckBook().getTitle());
        Assertions.assertEquals("RUB", snapshot.getCheckBook().getCurrencyCode());
        Assertions.assertNotNull(snapshot.getCheckBook().getInviteToken());
        Assertions.assertEquals(1, snapshot.getParticipants().size());
        Assertions.assertEquals("alice", snapshot.getParticipants().get(0).getDisplayName());
        Assertions.assertEquals(ParticipantType.REGISTERED, snapshot.getParticipants().get(0).getType());
    }

    @Test
    void addGuestParticipantRejectsDuplicateName() {
        TestContext context = new TestContext();
        CheckCommandService service = context.service;

        CheckSnapshot snapshot = service.createCheck("Trip", 1001L, "alice");
        service.addGuestParticipant(snapshot.getCheckBook().getId(), "Bob");

        ApiException exception = Assertions.assertThrows(ApiException.class, () ->
                service.addGuestParticipant(snapshot.getCheckBook().getId(), "Bob")
        );

        Assertions.assertEquals(ApiErrorCode.PARTICIPANT_NAME_CONFLICT, exception.getCode());
    }

    @Test
    void addRegisteredParticipantRejectsDuplicateUser() {
        TestContext context = new TestContext();
        CheckCommandService service = context.service;

        CheckSnapshot snapshot = service.createCheck("Trip", 1001L, "alice");
        service.addRegisteredParticipant(snapshot.getCheckBook().getId(), 1002L, "bob");

        ApiException exception = Assertions.assertThrows(ApiException.class, () ->
                service.addRegisteredParticipant(snapshot.getCheckBook().getId(), 1002L, "bob")
        );

        Assertions.assertEquals(ApiErrorCode.REGISTERED_PARTICIPANT_ALREADY_EXISTS, exception.getCode());
    }

    @Test
    void createCheckRejectsLimitOver100PerDay() {
        TestContext context = new TestContext();
        CheckCommandService service = context.service;

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
        TestContext context = new TestContext();
        CheckCommandService service = context.service;

        CheckSnapshot snapshot = service.createCheck("Trip", 1001L, "alice");

        ApiException exception = Assertions.assertThrows(ApiException.class, () ->
                service.addRegisteredParticipant(snapshot.getCheckBook().getId(), 1002L, "   ")
        );

        Assertions.assertEquals(ApiErrorCode.VALIDATION_ERROR, exception.getCode());
    }

    @Test
    void joinCheckByInviteTokenAddsRegisteredParticipant() {
        TestContext context = new TestContext();
        CheckSnapshot snapshot = context.service.createCheck("Trip", 1001L, "alice");

        Participant participant = context.service.joinCheckByInviteToken(
                snapshot.getCheckBook().getInviteToken(),
                1002L,
                "@bob"
        );

        Assertions.assertEquals("bob", participant.getDisplayName());
        Assertions.assertEquals(ParticipantType.REGISTERED, participant.getType());
    }

    @Test
    void mergeParticipantReassignsExpenseReferencesAndStoresHistory() {
        TestContext context = new TestContext();
        UUID checkId = UUID.randomUUID();
        UUID performerId = UUID.randomUUID();
        UUID sourceGuestId = UUID.randomUUID();
        UUID targetRegisteredId = UUID.randomUUID();

        context.checkRepository.save(new CheckBook(checkId, "Trip", UUID.randomUUID(), null, "RUB", true, OffsetDateTime.now()));
        context.participantRepository.save(new Participant(performerId, checkId, ParticipantType.REGISTERED, "owner", UUID.randomUUID(), null, OffsetDateTime.now().plusSeconds(1)));
        context.participantRepository.save(new Participant(sourceGuestId, checkId, ParticipantType.GUEST, "guest", null, null, OffsetDateTime.now().plusSeconds(2)));
        context.participantRepository.save(new Participant(targetRegisteredId, checkId, ParticipantType.REGISTERED, "alice", UUID.randomUUID(), null, OffsetDateTime.now().plusSeconds(3)));

        UUID expenseId = UUID.randomUUID();
        context.expenseRepository.save(new Expense(
                expenseId,
                checkId,
                900L,
                "RUB",
                sourceGuestId,
                "Dinner",
                null,
                null,
                null,
                ExpenseStatus.VALID,
                sourceGuestId,
                performerId,
                OffsetDateTime.now(),
                OffsetDateTime.now()
        ));
        context.expenseShareRepository.saveAll(java.util.Arrays.asList(
                new ExpenseShare(expenseId, sourceGuestId, 450L),
                new ExpenseShare(expenseId, targetRegisteredId, 450L)
        ));

        Participant mergedTarget = context.service.mergeParticipant(checkId, sourceGuestId, targetRegisteredId, performerId);

        Assertions.assertEquals(targetRegisteredId, mergedTarget.getId());
        Participant source = context.participantRepository.findById(sourceGuestId).get();
        Assertions.assertEquals(targetRegisteredId, source.getMergedIntoParticipantId());
        Expense updatedExpense = context.expenseRepository.findById(expenseId).get();
        Assertions.assertEquals(targetRegisteredId, updatedExpense.getPayerParticipantId());
        Assertions.assertEquals(targetRegisteredId, updatedExpense.getCreatedByParticipantId());
        Assertions.assertEquals(1, context.expenseShareRepository.findByExpenseId(expenseId).size());
        Assertions.assertEquals(900L, context.expenseShareRepository.findByExpenseId(expenseId).get(0).getShareMinor());
        Assertions.assertEquals(1, context.participantMergeRepository.records.size());
    }

    @Test
    void mergeParticipantRejectsRegisteredSource() {
        TestContext context = new TestContext();
        UUID checkId = UUID.randomUUID();
        UUID sourceRegisteredId = UUID.randomUUID();
        UUID targetRegisteredId = UUID.randomUUID();
        UUID performerId = UUID.randomUUID();

        context.checkRepository.save(new CheckBook(checkId, "Trip", UUID.randomUUID(), null, "RUB", true, OffsetDateTime.now()));
        context.participantRepository.save(new Participant(sourceRegisteredId, checkId, ParticipantType.REGISTERED, "alice", UUID.randomUUID(), null, OffsetDateTime.now().plusSeconds(1)));
        context.participantRepository.save(new Participant(targetRegisteredId, checkId, ParticipantType.REGISTERED, "bob", UUID.randomUUID(), null, OffsetDateTime.now().plusSeconds(2)));
        context.participantRepository.save(new Participant(performerId, checkId, ParticipantType.REGISTERED, "owner", UUID.randomUUID(), null, OffsetDateTime.now().plusSeconds(3)));

        ApiException exception = Assertions.assertThrows(ApiException.class, () ->
                context.service.mergeParticipant(checkId, sourceRegisteredId, targetRegisteredId, performerId)
        );

        Assertions.assertEquals(ApiErrorCode.PARTICIPANT_MERGE_INVALID, exception.getCode());
    }

    private static class TestContext {
        private final InMemoryAppUserRepository userRepository = new InMemoryAppUserRepository();
        private final InMemoryCheckBookRepository checkRepository = new InMemoryCheckBookRepository();
        private final InMemoryParticipantRepository participantRepository = new InMemoryParticipantRepository();
        private final InMemoryParticipantMergeRepository participantMergeRepository = new InMemoryParticipantMergeRepository();
        private final InMemoryExpenseRepository expenseRepository = new InMemoryExpenseRepository();
        private final InMemoryExpenseShareRepository expenseShareRepository = new InMemoryExpenseShareRepository();
        private final CheckCommandService service = new CheckCommandService(
                userRepository,
                checkRepository,
                participantRepository,
                participantMergeRepository,
                expenseRepository,
                expenseShareRepository
        );
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
        public Optional<CheckBook> findByInviteToken(String inviteToken) {
            for (CheckBook checkBook : checks.values()) {
                if (inviteToken.equals(checkBook.getInviteToken())) {
                    return Optional.of(checkBook);
                }
            }
            return Optional.empty();
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
        public Participant update(Participant participant) {
            for (int i = 0; i < participants.size(); i++) {
                if (participants.get(i).getId().equals(participant.getId())) {
                    participants.set(i, participant);
                    return participant;
                }
            }
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
        public Optional<Participant> findById(UUID participantId) {
            for (Participant participant : participants) {
                if (participant.getId().equals(participantId)) {
                    return Optional.of(participant);
                }
            }
            return Optional.empty();
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

    private static class InMemoryParticipantMergeRepository implements ParticipantMergeRepository {
        private final List<ParticipantMergeRecord> records = new ArrayList<ParticipantMergeRecord>();

        @Override
        public ParticipantMergeRecord save(ParticipantMergeRecord record) {
            records.add(record);
            return record;
        }
    }

    private static class InMemoryExpenseRepository implements ExpenseRepository {
        private final Map<UUID, Expense> expenses = new LinkedHashMap<UUID, Expense>();

        @Override
        public Expense save(Expense expense) {
            expenses.put(expense.getId(), expense);
            return expense;
        }

        @Override
        public Expense update(Expense expense) {
            expenses.put(expense.getId(), expense);
            return expense;
        }

        @Override
        public Optional<Expense> findById(UUID expenseId) {
            return Optional.ofNullable(expenses.get(expenseId));
        }

        @Override
        public List<Expense> findByCheckId(UUID checkId) {
            List<Expense> result = new ArrayList<Expense>();
            for (Expense expense : expenses.values()) {
                if (expense.getCheckId().equals(checkId)) {
                    result.add(expense);
                }
            }
            result.sort(Comparator.comparing(Expense::getCreatedAt));
            return result;
        }

        @Override
        public void deleteById(UUID expenseId) {
            expenses.remove(expenseId);
        }
    }

    private static class InMemoryExpenseShareRepository implements ExpenseShareRepository {
        private final List<ExpenseShare> shares = new ArrayList<ExpenseShare>();

        @Override
        public void saveAll(List<ExpenseShare> sharesToSave) {
            shares.addAll(sharesToSave);
        }

        @Override
        public List<ExpenseShare> findByExpenseId(UUID expenseId) {
            List<ExpenseShare> result = new ArrayList<ExpenseShare>();
            for (ExpenseShare share : shares) {
                if (share.getExpenseId().equals(expenseId)) {
                    result.add(share);
                }
            }
            return result;
        }

        @Override
        public void deleteByExpenseId(UUID expenseId) {
            shares.removeIf(share -> share.getExpenseId().equals(expenseId));
        }
    }
}

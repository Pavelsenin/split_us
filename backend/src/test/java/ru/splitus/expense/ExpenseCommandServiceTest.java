package ru.splitus.expense;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.splitus.check.CheckBook;
import ru.splitus.check.CheckBookRepository;
import ru.splitus.check.Participant;
import ru.splitus.check.ParticipantRepository;
import ru.splitus.check.ParticipantType;
import ru.splitus.error.ApiErrorCode;
import ru.splitus.error.ApiException;

class ExpenseCommandServiceTest {

    @Test
    void createExpenseSplitsAmountAndAssignsRemainderDeterministically() {
        Fixture fixture = new Fixture();

        ExpenseDetails details = fixture.service.createExpense(
                fixture.checkId,
                fixture.aliceId,
                1001L,
                "Dinner",
                null,
                Arrays.asList(fixture.bobId, fixture.aliceId, fixture.carolId),
                fixture.aliceId
        );

        Assertions.assertEquals(3, details.getShares().size());
        Assertions.assertEquals(335L, details.getShares().get(0).getShareMinor());
        Assertions.assertEquals(333L, details.getShares().get(1).getShareMinor());
        Assertions.assertEquals(333L, details.getShares().get(2).getShareMinor());
    }

    @Test
    void createExpenseRejectsUnknownSplitParticipant() {
        Fixture fixture = new Fixture();

        ApiException exception = Assertions.assertThrows(ApiException.class, () ->
                fixture.service.createExpense(
                        fixture.checkId,
                        fixture.aliceId,
                        1000L,
                        "Dinner",
                        null,
                        Arrays.asList(UUID.randomUUID()),
                        fixture.aliceId
                )
        );

        Assertions.assertEquals(ApiErrorCode.PARTICIPANT_DOES_NOT_BELONG_TO_CHECK, exception.getCode());
    }

    @Test
    void updateExpenseRejectsNonAuthor() {
        Fixture fixture = new Fixture();
        ExpenseDetails created = fixture.service.createExpense(
                fixture.checkId,
                fixture.aliceId,
                1200L,
                "Taxi",
                null,
                Arrays.asList(fixture.aliceId, fixture.bobId),
                fixture.aliceId
        );

        ApiException exception = Assertions.assertThrows(ApiException.class, () ->
                fixture.service.updateExpense(
                        created.getExpense().getId(),
                        Long.valueOf(1400L),
                        null,
                        null,
                        null,
                        null,
                        fixture.bobId
                )
        );

        Assertions.assertEquals(ApiErrorCode.EXPENSE_AUTHOR_MISMATCH, exception.getCode());
    }

    @Test
    void updateExpenseRebuildsShares() {
        Fixture fixture = new Fixture();
        ExpenseDetails created = fixture.service.createExpense(
                fixture.checkId,
                fixture.aliceId,
                1200L,
                "Taxi",
                null,
                Arrays.asList(fixture.aliceId, fixture.bobId),
                fixture.aliceId
        );

        ExpenseDetails updated = fixture.service.updateExpense(
                created.getExpense().getId(),
                Long.valueOf(1501L),
                "Late taxi",
                "text",
                Arrays.asList(fixture.aliceId, fixture.carolId),
                ExpenseStatus.REQUIRES_CLARIFICATION,
                fixture.aliceId
        );

        Assertions.assertEquals(1501L, updated.getExpense().getAmountMinor());
        Assertions.assertEquals("Late taxi", updated.getExpense().getComment());
        Assertions.assertEquals(ExpenseStatus.REQUIRES_CLARIFICATION, updated.getExpense().getStatus());
        Assertions.assertEquals(2, updated.getShares().size());
        Assertions.assertEquals(751L, updated.getShares().get(0).getShareMinor());
        Assertions.assertEquals(750L, updated.getShares().get(1).getShareMinor());
    }

    @Test
    void deleteExpenseRemovesExpenseAndShares() {
        Fixture fixture = new Fixture();
        ExpenseDetails created = fixture.service.createExpense(
                fixture.checkId,
                fixture.aliceId,
                1200L,
                "Taxi",
                null,
                Arrays.asList(fixture.aliceId, fixture.bobId),
                fixture.aliceId
        );

        fixture.service.deleteExpense(created.getExpense().getId(), fixture.aliceId);

        Assertions.assertFalse(fixture.expenseRepository.findById(created.getExpense().getId()).isPresent());
        Assertions.assertTrue(fixture.shareRepository.findByExpenseId(created.getExpense().getId()).isEmpty());
    }

    private static class Fixture {
        private final InMemoryCheckBookRepository checkRepository = new InMemoryCheckBookRepository();
        private final InMemoryParticipantRepository participantRepository = new InMemoryParticipantRepository();
        private final InMemoryExpenseRepository expenseRepository = new InMemoryExpenseRepository();
        private final InMemoryExpenseShareRepository shareRepository = new InMemoryExpenseShareRepository();
        private final ExpenseCommandService service = new ExpenseCommandService(
                checkRepository,
                participantRepository,
                expenseRepository,
                shareRepository
        );

        private final UUID checkId = UUID.randomUUID();
        private final UUID aliceId = UUID.randomUUID();
        private final UUID bobId = UUID.randomUUID();
        private final UUID carolId = UUID.randomUUID();

        private Fixture() {
            OffsetDateTime now = OffsetDateTime.now();
            checkRepository.save(new CheckBook(checkId, "Trip", UUID.randomUUID(), null, "RUB", true, now));
            participantRepository.save(new Participant(aliceId, checkId, ParticipantType.REGISTERED, "alice", UUID.randomUUID(), null, now.plusSeconds(1)));
            participantRepository.save(new Participant(bobId, checkId, ParticipantType.REGISTERED, "bob", UUID.randomUUID(), null, now.plusSeconds(2)));
            participantRepository.save(new Participant(carolId, checkId, ParticipantType.GUEST, "carol", null, null, now.plusSeconds(3)));
        }
    }

    private static class InMemoryExpenseRepository implements ExpenseRepository {
        private final Map<UUID, Expense> expenses = new HashMap<UUID, Expense>();

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
            return 0;
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
            return 0;
        }

        @Override
        public boolean existsByCheckIdAndDisplayName(UUID checkId, String displayName) {
            return false;
        }

        @Override
        public Optional<Participant> findActiveRegisteredParticipant(UUID checkId, UUID userId) {
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

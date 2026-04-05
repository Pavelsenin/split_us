package ru.splitus.settlement;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.splitus.check.CheckBook;
import ru.splitus.check.CheckBookRepository;
import ru.splitus.check.Participant;
import ru.splitus.check.ParticipantRepository;
import ru.splitus.check.ParticipantType;
import ru.splitus.error.ApiErrorCode;
import ru.splitus.error.ApiException;
import ru.splitus.expense.Expense;
import ru.splitus.expense.ExpenseRepository;
import ru.splitus.expense.ExpenseShare;
import ru.splitus.expense.ExpenseShareRepository;
import ru.splitus.expense.ExpenseStatus;

/**
 * Tests settlement execution service.
 */
class SettlementExecutionServiceTest {

    @Test
    void detectsStateChangeDuringCalculation() {
        Fixture fixture = new Fixture(new MutatingSolver());

        ApiException exception = Assertions.assertThrows(ApiException.class, () ->
                fixture.executionService.calculateStable(fixture.checkId)
        );

        Assertions.assertEquals(ApiErrorCode.SETTLEMENT_STATE_CHANGED, exception.getCode());
    }

    @Test
    void rejectsParallelCalculationForSameCheck() throws Exception {
        BlockingSolver solver = new BlockingSolver();
        Fixture fixture = new Fixture(solver);
        AtomicReference<Throwable> firstThreadFailure = new AtomicReference<Throwable>();

        Thread first = new Thread(() -> {
            try {
                fixture.executionService.calculateStable(fixture.checkId);
            } catch (Throwable throwable) {
                firstThreadFailure.set(throwable);
            }
        });
        first.start();

        Assertions.assertTrue(solver.started.await(5, TimeUnit.SECONDS));

        ApiException exception = Assertions.assertThrows(ApiException.class, () ->
                fixture.executionService.calculateStable(fixture.checkId)
        );

        Assertions.assertEquals(ApiErrorCode.SETTLEMENT_ALREADY_RUNNING, exception.getCode());

        solver.release.countDown();
        first.join(5000L);
        Assertions.assertNull(firstThreadFailure.get());
    }

    /**
     * Represents fixture.
     */
    private static class Fixture {
        private final InMemoryCheckBookRepository checkRepository = new InMemoryCheckBookRepository();
        private final InMemoryParticipantRepository participantRepository = new InMemoryParticipantRepository();
        private final InMemoryExpenseRepository expenseRepository = new InMemoryExpenseRepository();
        private final InMemoryExpenseShareRepository expenseShareRepository = new InMemoryExpenseShareRepository();
        private final SettlementQueryService queryService;
        private final SettlementExecutionService executionService;

        private final UUID checkId = UUID.randomUUID();
        private final UUID aliceId = UUID.randomUUID();
        private final UUID bobId = UUID.randomUUID();

        private Fixture(ExactSettlementSpikeSolver solver) {
            if (solver instanceof MutatingSolver) {
                ((MutatingSolver) solver).expenseRepository = expenseRepository;
            }
            this.queryService = new SettlementQueryService(
                    checkRepository,
                    participantRepository,
                    expenseRepository,
                    expenseShareRepository,
                    solver
            );
            this.executionService = new SettlementExecutionService(queryService);

            OffsetDateTime now = OffsetDateTime.now();
            checkRepository.save(new CheckBook(checkId, "Trip", UUID.randomUUID(), "token", null, "RUB", true, now));
            participantRepository.save(new Participant(aliceId, checkId, ParticipantType.REGISTERED, "alice", UUID.randomUUID(), null, now.plusSeconds(1)));
            participantRepository.save(new Participant(bobId, checkId, ParticipantType.REGISTERED, "bob", UUID.randomUUID(), null, now.plusSeconds(2)));

            UUID expenseId = UUID.randomUUID();
            expenseRepository.save(new Expense(
                    expenseId,
                    checkId,
                    1000L,
                    "RUB",
                    aliceId,
                    "Dinner",
                    null,
                    null,
                    null,
                    ExpenseStatus.VALID,
                    aliceId,
                    aliceId,
                    now.plusSeconds(3),
                    now.plusSeconds(3)
            ));
            expenseShareRepository.saveAll(java.util.Arrays.asList(
                    new ExpenseShare(expenseId, aliceId, 500L),
                    new ExpenseShare(expenseId, bobId, 500L)
            ));
        }
    }

    /**
     * Represents mutating solver.
     */
    private static class MutatingSolver extends ExactSettlementSpikeSolver {
        private InMemoryExpenseRepository expenseRepository;

        /**
         * Executes solve.
         */
        @Override
        public SettlementPlan solve(Map<String, Long> participantBalances) {
            if (expenseRepository != null) {
                Expense expense = expenseRepository.expenses.values().iterator().next();
                expenseRepository.update(new Expense(
                        expense.getId(),
                        expense.getCheckId(),
                        expense.getAmountMinor(),
                        expense.getCurrencyCode(),
                        expense.getPayerParticipantId(),
                        expense.getComment(),
                        expense.getSourceMessageText(),
                        expense.getTelegramChatId(),
                        expense.getTelegramMessageId(),
                        ExpenseStatus.INVALID,
                        expense.getCreatedByParticipantId(),
                        expense.getUpdatedByParticipantId(),
                        expense.getCreatedAt(),
                        OffsetDateTime.now()
                ));
            }
            return super.solve(participantBalances);
        }
    }

    /**
     * Represents blocking solver.
     */
    private static class BlockingSolver extends ExactSettlementSpikeSolver {
        private final CountDownLatch started = new CountDownLatch(1);
        private final CountDownLatch release = new CountDownLatch(1);

        /**
         * Executes solve.
         */
        @Override
        public SettlementPlan solve(Map<String, Long> participantBalances) {
            started.countDown();
            try {
                release.await(5, TimeUnit.SECONDS);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(exception);
            }
            return super.solve(participantBalances);
        }
    }

    /**
     * Represents in memory check book repository.
     */
    private static class InMemoryCheckBookRepository implements CheckBookRepository {
        private final Map<UUID, CheckBook> checks = new HashMap<UUID, CheckBook>();

        /**
         * Executes save.
         */
        @Override
        public CheckBook save(CheckBook checkBook) {
            checks.put(checkBook.getId(), checkBook);
            return checkBook;
        }

        /**
         * Finds by id.
         */
        @Override
        public Optional<CheckBook> findById(UUID checkId) {
            return Optional.ofNullable(checks.get(checkId));
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
    }

    /**
     * Represents in memory participant repository.
     */
    private static class InMemoryParticipantRepository implements ParticipantRepository {
        private final List<Participant> participants = new ArrayList<Participant>();

        /**
         * Executes save.
         */
        @Override
        public Participant save(Participant participant) {
            participants.add(participant);
            return participant;
        }

        /**
         * Executes update.
         */
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

        /**
         * Counts by check id.
         */
        @Override
        public int countByCheckId(UUID checkId) {
            return 0;
        }

        /**
         * Checks whether by check id and display name.
         */
        @Override
        public boolean existsByCheckIdAndDisplayName(UUID checkId, String displayName) {
            return false;
        }

        /**
         * Finds by id.
         */
        @Override
        public Optional<Participant> findById(UUID participantId) {
            return Optional.empty();
        }

        /**
         * Finds active registered participant.
         */
        @Override
        public Optional<Participant> findActiveRegisteredParticipant(UUID checkId, UUID userId) {
            return Optional.empty();
        }

        /**
         * Finds by check id.
         */
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

    /**
     * Represents in memory expense repository.
     */
    private static class InMemoryExpenseRepository implements ExpenseRepository {
        private final Map<UUID, Expense> expenses = new HashMap<UUID, Expense>();

        /**
         * Executes save.
         */
        @Override
        public Expense save(Expense expense) {
            expenses.put(expense.getId(), expense);
            return expense;
        }

        /**
         * Executes update.
         */
        @Override
        public Expense update(Expense expense) {
            expenses.put(expense.getId(), expense);
            return expense;
        }

        /**
         * Finds by id.
         */
        @Override
        public Optional<Expense> findById(UUID expenseId) {
            return Optional.ofNullable(expenses.get(expenseId));
        }

        /**
         * Finds by telegram message.
         */
        @Override
        public Optional<Expense> findByTelegramMessage(long telegramChatId, long telegramMessageId) {
            return Optional.empty();
        }

        /**
         * Finds by check id.
         */
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

        /**
         * Deletes by id.
         */
        @Override
        public void deleteById(UUID expenseId) {
            expenses.remove(expenseId);
        }
    }

    /**
     * Represents in memory expense share repository.
     */
    private static class InMemoryExpenseShareRepository implements ExpenseShareRepository {
        private final List<ExpenseShare> shares = new ArrayList<ExpenseShare>();

        /**
         * Executes save all.
         */
        @Override
        public void saveAll(List<ExpenseShare> sharesToSave) {
            shares.addAll(sharesToSave);
        }

        /**
         * Finds by expense id.
         */
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

        /**
         * Deletes by expense id.
         */
        @Override
        public void deleteByExpenseId(UUID expenseId) {
            shares.removeIf(share -> share.getExpenseId().equals(expenseId));
        }
    }
}




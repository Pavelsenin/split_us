package ru.splitus.web;

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
import ru.splitus.check.CheckBook;
import ru.splitus.check.CheckBookRepository;
import ru.splitus.check.Participant;
import ru.splitus.check.ParticipantRepository;
import ru.splitus.check.ParticipantType;
import ru.splitus.expense.Expense;
import ru.splitus.expense.ExpenseRepository;
import ru.splitus.expense.ExpenseShare;
import ru.splitus.expense.ExpenseShareRepository;
import ru.splitus.expense.ExpenseStatus;
import ru.splitus.settlement.ExactSettlementSpikeSolver;
import ru.splitus.settlement.SettlementQueryService;
import ru.splitus.web.dto.SettlementResponse;

class InternalSettlementControllerTest {

    @Test
    void returnsBalancesAndTransfersForCheckSettlement() {
        Fixture fixture = new Fixture();

        SettlementResponse response = fixture.controller.calculateSettlement(fixture.checkId);

        Assertions.assertEquals(2, response.getTransferCount());
        Assertions.assertEquals(3, response.getBalances().size());
        Assertions.assertEquals("alice", response.getBalances().get(0).getParticipant());
        Assertions.assertEquals(600L, response.getBalances().get(0).getBalanceMinor());
        Assertions.assertEquals("bob", response.getTransfers().get(0).getFromParticipant());
        Assertions.assertEquals("alice", response.getTransfers().get(0).getToParticipant());
        Assertions.assertEquals(300L, response.getTransfers().get(0).getAmountMinor());
    }

    private static class Fixture {
        private final InMemoryCheckBookRepository checkRepository = new InMemoryCheckBookRepository();
        private final InMemoryParticipantRepository participantRepository = new InMemoryParticipantRepository();
        private final InMemoryExpenseRepository expenseRepository = new InMemoryExpenseRepository();
        private final InMemoryExpenseShareRepository expenseShareRepository = new InMemoryExpenseShareRepository();
        private final SettlementQueryService settlementQueryService = new SettlementQueryService(
                checkRepository,
                participantRepository,
                expenseRepository,
                expenseShareRepository,
                new ExactSettlementSpikeSolver()
        );
        private final InternalSettlementController controller = new InternalSettlementController(settlementQueryService);

        private final UUID checkId = UUID.randomUUID();
        private final UUID aliceId = UUID.randomUUID();
        private final UUID bobId = UUID.randomUUID();
        private final UUID carolId = UUID.randomUUID();

        private Fixture() {
            OffsetDateTime now = OffsetDateTime.now();
            checkRepository.save(new CheckBook(checkId, "Trip", UUID.randomUUID(), "token", null, "RUB", true, now));
            participantRepository.save(new Participant(aliceId, checkId, ParticipantType.REGISTERED, "alice", UUID.randomUUID(), null, now.plusSeconds(1)));
            participantRepository.save(new Participant(bobId, checkId, ParticipantType.REGISTERED, "bob", UUID.randomUUID(), null, now.plusSeconds(2)));
            participantRepository.save(new Participant(carolId, checkId, ParticipantType.GUEST, "carol", null, null, now.plusSeconds(3)));

            UUID expenseId = UUID.randomUUID();
            expenseRepository.save(new Expense(
                    expenseId,
                    checkId,
                    900L,
                    "RUB",
                    aliceId,
                    "Dinner",
                    null,
                    null,
                    null,
                    ExpenseStatus.VALID,
                    aliceId,
                    aliceId,
                    now.plusSeconds(4),
                    now.plusSeconds(4)
            ));
            expenseShareRepository.saveAll(java.util.Arrays.asList(
                    new ExpenseShare(expenseId, aliceId, 300L),
                    new ExpenseShare(expenseId, bobId, 300L),
                    new ExpenseShare(expenseId, carolId, 300L)
            ));
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
            return 0;
        }

        @Override
        public boolean existsByCheckIdAndDisplayName(UUID checkId, String displayName) {
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
        public Optional<Expense> findByTelegramMessage(long telegramChatId, long telegramMessageId) {
            return Optional.empty();
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

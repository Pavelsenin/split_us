package ru.splitus.acceptance;

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
import ru.splitus.check.AppUser;
import ru.splitus.check.AppUserRepository;
import ru.splitus.check.CheckBook;
import ru.splitus.check.CheckBookRepository;
import ru.splitus.check.CheckCommandService;
import ru.splitus.check.Participant;
import ru.splitus.check.ParticipantMergeRecord;
import ru.splitus.check.ParticipantMergeRepository;
import ru.splitus.check.ParticipantRepository;
import ru.splitus.config.TelegramWebhookProperties;
import ru.splitus.expense.Expense;
import ru.splitus.expense.ExpenseCommandService;
import ru.splitus.expense.ExpenseDetails;
import ru.splitus.expense.ExpenseRepository;
import ru.splitus.expense.ExpenseShare;
import ru.splitus.expense.ExpenseShareRepository;
import ru.splitus.expense.ExpenseStatus;
import ru.splitus.settlement.ExactSettlementSpikeSolver;
import ru.splitus.settlement.SettlementExecutionService;
import ru.splitus.settlement.SettlementQueryService;
import ru.splitus.telegram.TelegramChat;
import ru.splitus.telegram.TelegramCommandService;
import ru.splitus.telegram.TelegramMessage;
import ru.splitus.telegram.TelegramUpdate;
import ru.splitus.telegram.TelegramUser;
import ru.splitus.telegram.TelegramWebhookResult;

/**
 * Covers end-to-end MVP scenarios across Telegram flows.
 */
class SplitUsScenarioTest {

    @Test
    void expenseRecoversFromClarificationAndReturnsToSettlement() {
        Fixture fixture = new Fixture();

        TelegramWebhookResult createResult = fixture.service.handleUpdate(fixture.messageUpdate(101L, 1001L, "alice", 1L, "/new_check Weekend"));
        Assertions.assertTrue(createResult.getOutgoingMessages().get(0).getText().contains("Weekend"));

        String inviteToken = fixture.firstCheck().getInviteToken();

        TelegramWebhookResult joinResult = fixture.service.handleUpdate(fixture.messageUpdate(102L, 1002L, "bob", 1L, "/start join_" + inviteToken));
        Assertions.assertTrue(joinResult.getOutgoingMessages().get(0).getText().contains("@bob"));

        TelegramWebhookResult addGuestResult = fixture.service.handleUpdate(
                fixture.messageUpdate(104L, 1002L, "bob", 1L, "/add_guest " + inviteToken + " Charlie")
        );
        Assertions.assertTrue(addGuestResult.getOutgoingMessages().get(0).getText().contains("Charlie"));

        TelegramWebhookResult addExpenseResult = fixture.service.handleUpdate(
                fixture.messageUpdate(104L, 1002L, "bob", 2L, "/add_expense " + inviteToken + " 1800 me,Charlie | Dinner")
        );
        Assertions.assertTrue(addExpenseResult.getOutgoingMessages().get(0).getText().contains("1800"));

        Expense initialExpense = fixture.firstExpense().getExpense();
        Assertions.assertEquals(ExpenseStatus.VALID, initialExpense.getStatus());

        TelegramWebhookResult invalidEditResult = fixture.service.handleUpdate(
                fixture.editedMessageUpdate(104L, 1002L, "bob", 2L, "/add_expense " + inviteToken + " broken_payload")
        );
        Assertions.assertEquals(1, invalidEditResult.getOutgoingMessages().size());
        Assertions.assertEquals(ExpenseStatus.REQUIRES_CLARIFICATION, fixture.firstExpense().getExpense().getStatus());

        TelegramWebhookResult fixedEditResult = fixture.service.handleUpdate(
                fixture.editedMessageUpdate(104L, 1002L, "bob", 2L, "/add_expense " + inviteToken + " 2100 me,Charlie | Fixed dinner")
        );
        Assertions.assertEquals(1, fixedEditResult.getOutgoingMessages().size());

        ExpenseDetails recoveredExpense = fixture.firstExpense();
        Assertions.assertEquals(ExpenseStatus.VALID, recoveredExpense.getExpense().getStatus());
        Assertions.assertEquals(2100L, recoveredExpense.getExpense().getAmountMinor());
        Assertions.assertEquals("Fixed dinner", recoveredExpense.getExpense().getComment());

        TelegramWebhookResult settlementResult = fixture.service.handleUpdate(
                fixture.messageUpdate(104L, 1002L, "bob", 3L, "/settle " + inviteToken)
        );
        Assertions.assertTrue(settlementResult.getOutgoingMessages().get(0).getText().contains("Charlie -> bob: 1050"));
    }

    /**
     * Provides a full in-memory service graph for scenario tests.
     */
    private static final class Fixture {
        private final InMemoryAppUserRepository appUserRepository = new InMemoryAppUserRepository();
        private final InMemoryCheckBookRepository checkBookRepository = new InMemoryCheckBookRepository();
        private final InMemoryParticipantRepository participantRepository = new InMemoryParticipantRepository();
        private final InMemoryParticipantMergeRepository participantMergeRepository = new InMemoryParticipantMergeRepository();
        private final InMemoryExpenseRepository expenseRepository = new InMemoryExpenseRepository();
        private final InMemoryExpenseShareRepository expenseShareRepository = new InMemoryExpenseShareRepository();
        private final CheckCommandService checkCommandService = new CheckCommandService(
                appUserRepository,
                checkBookRepository,
                participantRepository,
                participantMergeRepository,
                expenseRepository,
                expenseShareRepository
        );
        private final ExpenseCommandService expenseCommandService = new ExpenseCommandService(
                checkBookRepository,
                participantRepository,
                expenseRepository,
                expenseShareRepository
        );
        private final SettlementQueryService settlementQueryService = new SettlementQueryService(
                checkBookRepository,
                participantRepository,
                expenseRepository,
                expenseShareRepository,
                new ExactSettlementSpikeSolver()
        );
        private final SettlementExecutionService settlementExecutionService = new SettlementExecutionService(settlementQueryService);
        private final TelegramCommandService service;

        /**
         * Creates a new fixture instance.
         */
        private Fixture() {
            TelegramWebhookProperties properties = new TelegramWebhookProperties();
            properties.setBotUsername("splitus_bot");
            this.service = new TelegramCommandService(
                    checkCommandService,
                    expenseCommandService,
                    settlementExecutionService,
                    properties
            );
        }

        /**
         * Creates a standard message update.
         */
        private TelegramUpdate messageUpdate(Long chatId, Long userId, String username, Long messageId, String text) {
            TelegramUser user = new TelegramUser();
            user.setId(userId);
            user.setUsername(username);
            user.setFirstName("User");

            TelegramChat chat = new TelegramChat();
            chat.setId(chatId);
            chat.setType("private");

            TelegramMessage message = new TelegramMessage();
            message.setMessageId(messageId);
            message.setChat(chat);
            message.setFrom(user);
            message.setText(text);

            TelegramUpdate update = new TelegramUpdate();
            update.setUpdateId(messageId);
            update.setMessage(message);
            return update;
        }

        /**
         * Creates an edited message update.
         */
        private TelegramUpdate editedMessageUpdate(Long chatId, Long userId, String username, Long messageId, String text) {
            TelegramUpdate update = messageUpdate(chatId, userId, username, messageId, text);
            update.setEditedMessage(update.getMessage());
            update.setMessage(null);
            return update;
        }

        /**
         * Returns the first stored check.
         */
        private CheckBook firstCheck() {
            return checkBookRepository.checks.values().iterator().next();
        }

        /**
         * Returns the first stored expense with shares.
         */
        private ExpenseDetails firstExpense() {
            Expense expense = expenseRepository.expenses.values().iterator().next();
            return new ExpenseDetails(expense, expenseShareRepository.findByExpenseId(expense.getId()));
        }
    }

    /**
     * Stores app users in memory.
     */
    private static final class InMemoryAppUserRepository implements AppUserRepository {
        private final Map<Long, AppUser> byTelegramId = new HashMap<Long, AppUser>();

        /**
         * Finds by telegram user id.
         */
        @Override
        public Optional<AppUser> findByTelegramUserId(long telegramUserId) {
            return Optional.ofNullable(byTelegramId.get(Long.valueOf(telegramUserId)));
        }

        /**
         * Saves the user.
         */
        @Override
        public AppUser save(AppUser user) {
            byTelegramId.put(Long.valueOf(user.getTelegramUserId()), user);
            return user;
        }

        /**
         * Updates telegram username.
         */
        @Override
        public AppUser updateUsername(AppUser user, String telegramUsername) {
            AppUser updated = new AppUser(user.getId(), user.getTelegramUserId(), telegramUsername, user.getRegisteredAt(), OffsetDateTime.now());
            byTelegramId.put(Long.valueOf(updated.getTelegramUserId()), updated);
            return updated;
        }
    }

    /**
     * Stores checks in memory.
     */
    private static final class InMemoryCheckBookRepository implements CheckBookRepository {
        private final Map<UUID, CheckBook> checks = new LinkedHashMap<UUID, CheckBook>();

        /**
         * Saves the check.
         */
        @Override
        public CheckBook save(CheckBook checkBook) {
            checks.put(checkBook.getId(), checkBook);
            return checkBook;
        }

        /**
         * Finds a check by id.
         */
        @Override
        public Optional<CheckBook> findById(UUID checkId) {
            return Optional.ofNullable(checks.get(checkId));
        }

        /**
         * Finds a check by invite token.
         */
        @Override
        public Optional<CheckBook> findByInviteToken(String inviteToken) {
            for (CheckBook checkBook : checks.values()) {
                if (inviteToken.equals(checkBook.getInviteToken())) {
                    return Optional.of(checkBook);
                }
            }
            return Optional.empty();
        }

        /**
         * Counts checks created by owner since a timestamp.
         */
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

    /**
     * Stores participants in memory.
     */
    private static final class InMemoryParticipantRepository implements ParticipantRepository {
        private final List<Participant> participants = new ArrayList<Participant>();

        /**
         * Saves the participant.
         */
        @Override
        public Participant save(Participant participant) {
            participants.add(participant);
            return participant;
        }

        /**
         * Updates the participant.
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
         * Counts active participants in a check.
         */
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

        /**
         * Checks whether an active participant name already exists in a check.
         */
        @Override
        public boolean existsByCheckIdAndDisplayName(UUID checkId, String displayName) {
            for (Participant participant : participants) {
                if (participant.getCheckId().equals(checkId) && participant.isActive() && participant.getDisplayName().equals(displayName)) {
                    return true;
                }
            }
            return false;
        }

        /**
         * Finds a participant by id.
         */
        @Override
        public Optional<Participant> findById(UUID participantId) {
            for (Participant participant : participants) {
                if (participant.getId().equals(participantId)) {
                    return Optional.of(participant);
                }
            }
            return Optional.empty();
        }

        /**
         * Finds an active registered participant.
         */
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

        /**
         * Lists participants by check id.
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
     * Stores merge records in memory.
     */
    private static final class InMemoryParticipantMergeRepository implements ParticipantMergeRepository {
        /**
         * Saves the merge record.
         */
        @Override
        public ParticipantMergeRecord save(ParticipantMergeRecord record) {
            return record;
        }
    }

    /**
     * Stores expenses in memory.
     */
    private static final class InMemoryExpenseRepository implements ExpenseRepository {
        private final Map<UUID, Expense> expenses = new LinkedHashMap<UUID, Expense>();

        /**
         * Saves the expense.
         */
        @Override
        public Expense save(Expense expense) {
            expenses.put(expense.getId(), expense);
            return expense;
        }

        /**
         * Updates the expense.
         */
        @Override
        public Expense update(Expense expense) {
            expenses.put(expense.getId(), expense);
            return expense;
        }

        /**
         * Finds an expense by id.
         */
        @Override
        public Optional<Expense> findById(UUID expenseId) {
            return Optional.ofNullable(expenses.get(expenseId));
        }

        /**
         * Finds an expense by telegram message coordinates.
         */
        @Override
        public Optional<Expense> findByTelegramMessage(long telegramChatId, long telegramMessageId) {
            for (Expense expense : expenses.values()) {
                if (expense.getTelegramChatId() != null
                        && expense.getTelegramMessageId() != null
                        && expense.getTelegramChatId().longValue() == telegramChatId
                        && expense.getTelegramMessageId().longValue() == telegramMessageId) {
                    return Optional.of(expense);
                }
            }
            return Optional.empty();
        }

        /**
         * Lists expenses by check id.
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
         * Deletes an expense by id.
         */
        @Override
        public void deleteById(UUID expenseId) {
            expenses.remove(expenseId);
        }
    }

    /**
     * Stores expense shares in memory.
     */
    private static final class InMemoryExpenseShareRepository implements ExpenseShareRepository {
        private final List<ExpenseShare> shares = new ArrayList<ExpenseShare>();

        /**
         * Saves shares.
         */
        @Override
        public void saveAll(List<ExpenseShare> sharesToSave) {
            shares.addAll(sharesToSave);
        }

        /**
         * Finds shares by expense id.
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
         * Deletes shares by expense id.
         */
        @Override
        public void deleteByExpenseId(UUID expenseId) {
            shares.removeIf(share -> share.getExpenseId().equals(expenseId));
        }
    }
}

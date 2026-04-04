package ru.splitus.telegram;

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
import ru.splitus.expense.ExpenseRepository;
import ru.splitus.expense.ExpenseShare;
import ru.splitus.expense.ExpenseShareRepository;

class TelegramCommandServiceTest {

    @Test
    void newCheckCommandCreatesCheckAndReturnsInviteLink() {
        Fixture fixture = new Fixture();

        TelegramWebhookResult result = fixture.service.handleUpdate(update(101L, 1001L, "alice", "/new_check Weekend"));

        Assertions.assertTrue(result.isAccepted());
        Assertions.assertEquals(1, result.getOutgoingMessages().size());
        Assertions.assertTrue(result.getOutgoingMessages().get(0).getText().contains("Weekend"));
        Assertions.assertTrue(result.getOutgoingMessages().get(0).getText().contains("https://t.me/splitus_bot?start=join_"));
    }

    @Test
    void commandWithBotMentionIsParsedForCurrentBot() {
        Fixture fixture = new Fixture();

        TelegramWebhookResult result = fixture.service.handleUpdate(update(101L, 1001L, "alice", "/new_check@splitus_bot Weekend"));

        Assertions.assertEquals(1, result.getOutgoingMessages().size());
        Assertions.assertTrue(result.getOutgoingMessages().get(0).getText().contains("Weekend"));
    }

    @Test
    void startJoinCommandAddsParticipantByInviteToken() {
        Fixture fixture = new Fixture();
        String inviteToken = fixture.checkCommandService.createCheck("Trip", 1001L, "alice").getCheckBook().getInviteToken();

        TelegramWebhookResult result = fixture.service.handleUpdate(update(102L, 1002L, "bob", "/start join_" + inviteToken));

        Assertions.assertTrue(result.getOutgoingMessages().get(0).getText().contains("@bob"));
    }

    @Test
    void startJoinCommandRejectsMissingUsername() {
        Fixture fixture = new Fixture();
        String inviteToken = fixture.checkCommandService.createCheck("Trip", 1001L, "alice").getCheckBook().getInviteToken();

        TelegramWebhookResult result = fixture.service.handleUpdate(update(103L, 1002L, "   ", "/start join_" + inviteToken));

        Assertions.assertTrue(result.getOutgoingMessages().get(0).getText().contains("username"));
    }

    @Test
    void addGuestCommandCreatesGuestForRegisteredParticipant() {
        Fixture fixture = new Fixture();
        String inviteToken = fixture.checkCommandService.createCheck("Trip", 1001L, "alice").getCheckBook().getInviteToken();
        fixture.checkCommandService.joinCheckByInviteToken(inviteToken, 1002L, "bob");

        TelegramWebhookResult result = fixture.service.handleUpdate(update(104L, 1002L, "bob", "/add_guest " + inviteToken + " Charlie"));

        Assertions.assertEquals(1, result.getOutgoingMessages().size());
        Assertions.assertTrue(result.getOutgoingMessages().get(0).getText().contains("Charlie"));
    }

    @Test
    void addExpenseCommandCreatesExpenseFromTelegramMessage() {
        Fixture fixture = new Fixture();
        String inviteToken = fixture.checkCommandService.createCheck("Trip", 1001L, "alice").getCheckBook().getInviteToken();
        fixture.checkCommandService.joinCheckByInviteToken(inviteToken, 1002L, "bob");
        fixture.checkCommandService.addGuestParticipantByInviteToken(inviteToken, 1002L, "bob", "Charlie");

        TelegramWebhookResult result = fixture.service.handleUpdate(
                update(104L, 1002L, "bob", "/add_expense " + inviteToken + " 1500 me,Charlie | Dinner")
        );

        Assertions.assertEquals(1, result.getOutgoingMessages().size());
        Assertions.assertTrue(result.getOutgoingMessages().get(0).getText().contains("1500"));
        Assertions.assertEquals(1, fixture.expenseRepository.expenses.size());
        Expense expense = fixture.expenseRepository.expenses.values().iterator().next();
        Assertions.assertTrue(result.getOutgoingMessages().get(0).getText().contains(expense.getId().toString()));
        Assertions.assertEquals(Long.valueOf(104L), expense.getTelegramChatId());
        Assertions.assertEquals(Long.valueOf(1L), expense.getTelegramMessageId());
        Assertions.assertEquals("/add_expense " + inviteToken + " 1500 me,Charlie | Dinner", expense.getSourceMessageText());
        Assertions.assertEquals(2, fixture.expenseShareRepository.findByExpenseId(expense.getId()).size());
    }

    @Test
    void listExpensesCommandReturnsCreatedExpenses() {
        Fixture fixture = new Fixture();
        String inviteToken = fixture.checkCommandService.createCheck("Trip", 1001L, "alice").getCheckBook().getInviteToken();
        fixture.checkCommandService.joinCheckByInviteToken(inviteToken, 1002L, "bob");

        fixture.service.handleUpdate(update(104L, 1002L, "bob", "/add_expense " + inviteToken + " 1500 me | Dinner"));
        Expense expense = fixture.expenseRepository.expenses.values().iterator().next();

        TelegramWebhookResult result = fixture.service.handleUpdate(
                update(104L, 1002L, "bob", "/list_expenses " + inviteToken)
        );

        Assertions.assertEquals(1, result.getOutgoingMessages().size());
        Assertions.assertTrue(result.getOutgoingMessages().get(0).getText().contains(expense.getId().toString()));
        Assertions.assertTrue(result.getOutgoingMessages().get(0).getText().contains("Dinner"));
    }

    @Test
    void updateExpenseCommandUpdatesAmountCommentAndSplit() {
        Fixture fixture = new Fixture();
        String inviteToken = fixture.checkCommandService.createCheck("Trip", 1001L, "alice").getCheckBook().getInviteToken();
        fixture.checkCommandService.joinCheckByInviteToken(inviteToken, 1002L, "bob");
        fixture.checkCommandService.addGuestParticipantByInviteToken(inviteToken, 1002L, "bob", "Charlie");

        fixture.service.handleUpdate(update(104L, 1002L, "bob", "/add_expense " + inviteToken + " 1500 me | Dinner"));
        Expense expense = fixture.expenseRepository.expenses.values().iterator().next();

        TelegramWebhookResult result = fixture.service.handleUpdate(
                update(104L, 1002L, "bob", "/update_expense " + expense.getId() + " 2100 me,Charlie | Team dinner")
        );

        Expense updatedExpense = fixture.expenseRepository.findById(expense.getId()).get();
        Assertions.assertEquals(1, result.getOutgoingMessages().size());
        Assertions.assertTrue(result.getOutgoingMessages().get(0).getText().contains(expense.getId().toString()));
        Assertions.assertEquals(2100L, updatedExpense.getAmountMinor());
        Assertions.assertEquals("Team dinner", updatedExpense.getComment());
        Assertions.assertEquals("/update_expense " + expense.getId() + " 2100 me,Charlie | Team dinner", updatedExpense.getSourceMessageText());
        Assertions.assertEquals(2, fixture.expenseShareRepository.findByExpenseId(expense.getId()).size());
    }

    @Test
    void editedAddExpenseMessageSynchronizesExistingExpense() {
        Fixture fixture = new Fixture();
        String inviteToken = fixture.checkCommandService.createCheck("Trip", 1001L, "alice").getCheckBook().getInviteToken();
        fixture.checkCommandService.joinCheckByInviteToken(inviteToken, 1002L, "bob");
        fixture.checkCommandService.addGuestParticipantByInviteToken(inviteToken, 1002L, "bob", "Charlie");

        fixture.service.handleUpdate(update(104L, 1002L, "bob", "/add_expense " + inviteToken + " 1500 me | Dinner"));
        Expense expense = fixture.expenseRepository.expenses.values().iterator().next();

        TelegramWebhookResult result = fixture.service.handleUpdate(
                editedUpdate(104L, 1002L, "bob", 1L, "/add_expense " + inviteToken + " 2200 me,Charlie | Edited dinner")
        );

        Expense updatedExpense = fixture.expenseRepository.findById(expense.getId()).get();
        Assertions.assertEquals(1, result.getOutgoingMessages().size());
        Assertions.assertTrue(result.getOutgoingMessages().get(0).getText().contains("синхронизирован"));
        Assertions.assertEquals(2200L, updatedExpense.getAmountMinor());
        Assertions.assertEquals("Edited dinner", updatedExpense.getComment());
        Assertions.assertEquals("/add_expense " + inviteToken + " 2200 me,Charlie | Edited dinner", updatedExpense.getSourceMessageText());
        Assertions.assertEquals(2, fixture.expenseShareRepository.findByExpenseId(expense.getId()).size());
    }

    @Test
    void deleteExpenseCommandRemovesOwnExpense() {
        Fixture fixture = new Fixture();
        String inviteToken = fixture.checkCommandService.createCheck("Trip", 1001L, "alice").getCheckBook().getInviteToken();
        fixture.checkCommandService.joinCheckByInviteToken(inviteToken, 1002L, "bob");

        fixture.service.handleUpdate(update(104L, 1002L, "bob", "/add_expense " + inviteToken + " 1500 me | Dinner"));
        Expense expense = fixture.expenseRepository.expenses.values().iterator().next();

        TelegramWebhookResult result = fixture.service.handleUpdate(
                update(104L, 1002L, "bob", "/delete_expense " + expense.getId())
        );

        Assertions.assertEquals(1, result.getOutgoingMessages().size());
        Assertions.assertTrue(result.getOutgoingMessages().get(0).getText().contains(expense.getId().toString()));
        Assertions.assertFalse(fixture.expenseRepository.findById(expense.getId()).isPresent());
    }

    @Test
    void addExpenseCommandRejectsUnknownParticipant() {
        Fixture fixture = new Fixture();
        String inviteToken = fixture.checkCommandService.createCheck("Trip", 1001L, "alice").getCheckBook().getInviteToken();
        fixture.checkCommandService.joinCheckByInviteToken(inviteToken, 1002L, "bob");

        TelegramWebhookResult result = fixture.service.handleUpdate(
                update(104L, 1002L, "bob", "/add_expense " + inviteToken + " 1500 Ghost | Dinner")
        );

        Assertions.assertEquals(1, result.getOutgoingMessages().size());
        Assertions.assertTrue(result.getOutgoingMessages().get(0).getText().contains("не найден"));
    }

    @Test
    void foreignBotCommandIsIgnored() {
        Fixture fixture = new Fixture();

        TelegramWebhookResult result = fixture.service.handleUpdate(update(105L, 1001L, "alice", "/new_check@other_bot Weekend"));

        Assertions.assertTrue(result.getOutgoingMessages().isEmpty());
    }

    private TelegramUpdate update(Long chatId, Long userId, String username, String text) {
        return update(chatId, userId, username, 1L, text);
    }

    private TelegramUpdate update(Long chatId, Long userId, String username, Long messageId, String text) {
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
        update.setUpdateId(1L);
        update.setMessage(message);
        return update;
    }

    private TelegramUpdate editedUpdate(Long chatId, Long userId, String username, Long messageId, String text) {
        TelegramUpdate update = update(chatId, userId, username, messageId, text);
        update.setEditedMessage(update.getMessage());
        update.setMessage(null);
        return update;
    }

    private static class Fixture {
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
        private final TelegramCommandService service;

        private Fixture() {
            TelegramWebhookProperties properties = new TelegramWebhookProperties();
            properties.setBotUsername("splitus_bot");
            this.service = new TelegramCommandService(checkCommandService, expenseCommandService, properties);
        }
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
        @Override
        public ParticipantMergeRecord save(ParticipantMergeRecord record) {
            return record;
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

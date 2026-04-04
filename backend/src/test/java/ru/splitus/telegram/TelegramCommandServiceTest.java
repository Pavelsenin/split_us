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
import ru.splitus.check.ParticipantType;
import ru.splitus.config.TelegramWebhookProperties;
import ru.splitus.expense.ExpenseRepository;
import ru.splitus.expense.ExpenseShareRepository;

class TelegramCommandServiceTest {

    @Test
    void newCheckCommandCreatesCheckAndReturnsInviteLink() {
        Fixture fixture = new Fixture();

        TelegramWebhookResult result = fixture.service.handleUpdate(update(101L, 1001L, "alice", "/new_check Weekend"));

        Assertions.assertTrue(result.isAccepted());
        Assertions.assertEquals(1, result.getOutgoingMessages().size());
        Assertions.assertTrue(result.getOutgoingMessages().get(0).getText().contains("Чек \"Weekend\" создан."));
        Assertions.assertTrue(result.getOutgoingMessages().get(0).getText().contains("https://t.me/splitus_bot?start=join_"));
    }

    @Test
    void startJoinCommandAddsParticipantByInviteToken() {
        Fixture fixture = new Fixture();
        String inviteToken = fixture.checkCommandService.createCheck("Trip", 1001L, "alice").getCheckBook().getInviteToken();

        TelegramWebhookResult result = fixture.service.handleUpdate(update(102L, 1002L, "bob", "/start join_" + inviteToken));

        Assertions.assertTrue(result.getOutgoingMessages().get(0).getText().contains("Вы присоединились к чеку как @bob."));
    }

    @Test
    void startJoinCommandRejectsMissingUsername() {
        Fixture fixture = new Fixture();
        String inviteToken = fixture.checkCommandService.createCheck("Trip", 1001L, "alice").getCheckBook().getInviteToken();

        TelegramWebhookResult result = fixture.service.handleUpdate(update(103L, 1002L, "   ", "/start join_" + inviteToken));

        Assertions.assertTrue(result.getOutgoingMessages().get(0).getText().contains("username"));
    }

    private TelegramUpdate update(Long chatId, Long userId, String username, String text) {
        TelegramUser user = new TelegramUser();
        user.setId(userId);
        user.setUsername(username);
        user.setFirstName("User");

        TelegramChat chat = new TelegramChat();
        chat.setId(chatId);
        chat.setType("private");

        TelegramMessage message = new TelegramMessage();
        message.setMessageId(1L);
        message.setChat(chat);
        message.setFrom(user);
        message.setText(text);

        TelegramUpdate update = new TelegramUpdate();
        update.setUpdateId(1L);
        update.setMessage(message);
        return update;
    }

    private static class Fixture {
        private final InMemoryAppUserRepository appUserRepository = new InMemoryAppUserRepository();
        private final InMemoryCheckBookRepository checkBookRepository = new InMemoryCheckBookRepository();
        private final InMemoryParticipantRepository participantRepository = new InMemoryParticipantRepository();
        private final InMemoryParticipantMergeRepository participantMergeRepository = new InMemoryParticipantMergeRepository();
        private final NoOpExpenseRepository expenseRepository = new NoOpExpenseRepository();
        private final NoOpExpenseShareRepository expenseShareRepository = new NoOpExpenseShareRepository();
        private final CheckCommandService checkCommandService = new CheckCommandService(
                appUserRepository,
                checkBookRepository,
                participantRepository,
                participantMergeRepository,
                expenseRepository,
                expenseShareRepository
        );
        private final TelegramCommandService service;

        private Fixture() {
            TelegramWebhookProperties properties = new TelegramWebhookProperties();
            properties.setBotUsername("splitus_bot");
            this.service = new TelegramCommandService(checkCommandService, properties);
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

    private static class NoOpExpenseRepository implements ExpenseRepository {
        @Override
        public ru.splitus.expense.Expense save(ru.splitus.expense.Expense expense) {
            return expense;
        }

        @Override
        public ru.splitus.expense.Expense update(ru.splitus.expense.Expense expense) {
            return expense;
        }

        @Override
        public Optional<ru.splitus.expense.Expense> findById(UUID expenseId) {
            return Optional.empty();
        }

        @Override
        public List<ru.splitus.expense.Expense> findByCheckId(UUID checkId) {
            return java.util.Collections.emptyList();
        }

        @Override
        public void deleteById(UUID expenseId) {
        }
    }

    private static class NoOpExpenseShareRepository implements ExpenseShareRepository {
        @Override
        public void saveAll(List<ru.splitus.expense.ExpenseShare> shares) {
        }

        @Override
        public List<ru.splitus.expense.ExpenseShare> findByExpenseId(UUID expenseId) {
            return java.util.Collections.emptyList();
        }

        @Override
        public void deleteByExpenseId(UUID expenseId) {
        }
    }
}

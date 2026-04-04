package ru.splitus.telegram;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ru.splitus.check.CheckCommandService;
import ru.splitus.check.CheckSnapshot;
import ru.splitus.check.Participant;
import ru.splitus.config.TelegramWebhookProperties;
import ru.splitus.error.ApiErrorCode;
import ru.splitus.error.ApiException;
import ru.splitus.expense.ExpenseCommandService;
import ru.splitus.expense.ExpenseDetails;

@Service
public class TelegramCommandService {

    private final CheckCommandService checkCommandService;
    private final ExpenseCommandService expenseCommandService;
    private final TelegramWebhookProperties telegramWebhookProperties;

    public TelegramCommandService(
            CheckCommandService checkCommandService,
            ExpenseCommandService expenseCommandService,
            TelegramWebhookProperties telegramWebhookProperties) {
        this.checkCommandService = checkCommandService;
        this.expenseCommandService = expenseCommandService;
        this.telegramWebhookProperties = telegramWebhookProperties;
    }

    public TelegramWebhookResult handleUpdate(TelegramUpdate update) {
        if (update == null || update.getMessage() == null || update.getMessage().getChat() == null) {
            return emptyResult();
        }

        Long chatId = update.getMessage().getChat().getId();
        String text = update.getMessage().getText();
        if (text == null || text.trim().isEmpty()) {
            return emptyResult();
        }

        try {
            ParsedCommand command = parseCommand(text.trim());
            if (command == null || command.ignored) {
                return emptyResult();
            }

            if ("new_check".equals(command.name)) {
                return handleNewCheck(update, command.arguments);
            }
            if ("start".equals(command.name)) {
                return handleStart(update, command.arguments);
            }
            if ("add_guest".equals(command.name)) {
                return handleAddGuest(update, command.arguments);
            }
            if ("add_expense".equals(command.name)) {
                return handleAddExpense(update, command.arguments);
            }
            if ("list_expenses".equals(command.name)) {
                return handleListExpenses(update, command.arguments);
            }
            if ("update_expense".equals(command.name)) {
                return handleUpdateExpense(update, command.arguments);
            }
            if ("delete_expense".equals(command.name)) {
                return handleDeleteExpense(update, command.arguments);
            }
            return reply(chatId, "Команда не поддерживается. Сейчас доступны /new_check, /start join_<token>, /add_guest, /add_expense, /list_expenses, /update_expense и /delete_expense.");
        } catch (ApiException exception) {
            return reply(chatId, exception.getMessage());
        }
    }

    private TelegramWebhookResult handleNewCheck(TelegramUpdate update, String arguments) {
        TelegramMessage message = update.getMessage();
        TelegramUser from = requiredUser(message);
        if (arguments.isEmpty()) {
            return reply(message.getChat().getId(), "Укажите название: /new_check Поход в Питер");
        }

        CheckSnapshot snapshot = checkCommandService.createCheck(arguments, from.getId().longValue(), from.getUsername());
        String deepLink = "https://t.me/" + telegramWebhookProperties.getBotUsername() + "?start=join_" + snapshot.getCheckBook().getInviteToken();
        String responseText = "Чек \"" + snapshot.getCheckBook().getTitle() + "\" создан.\n"
                + "Ссылка для присоединения: " + deepLink + "\n"
                + "Fallback для MVP: создайте группу вручную и добавьте туда бота как администратора.";
        return reply(message.getChat().getId(), responseText);
    }

    private TelegramWebhookResult handleStart(TelegramUpdate update, String arguments) {
        TelegramMessage message = update.getMessage();
        TelegramUser from = requiredUser(message);
        if (!arguments.startsWith("join_")) {
            return reply(message.getChat().getId(), "Неизвестный deep link. Ожидался формат /start join_<token>.");
        }

        String inviteToken = arguments.substring("join_".length()).trim();
        Participant participant = checkCommandService.joinCheckByInviteToken(inviteToken, from.getId().longValue(), from.getUsername());
        return reply(message.getChat().getId(), "Вы присоединились к чеку как @" + participant.getDisplayName() + ".");
    }

    private TelegramWebhookResult handleAddGuest(TelegramUpdate update, String arguments) {
        TelegramMessage message = update.getMessage();
        TelegramUser from = requiredUser(message);
        int separator = arguments.indexOf(' ');
        if (separator <= 0 || separator == arguments.length() - 1) {
            return reply(message.getChat().getId(), "Используйте /add_guest <invite_token> <имя гостя>.");
        }

        String inviteToken = arguments.substring(0, separator).trim();
        String guestName = arguments.substring(separator + 1).trim();
        Participant guest = checkCommandService.addGuestParticipantByInviteToken(
                inviteToken,
                from.getId().longValue(),
                from.getUsername(),
                guestName
        );
        return reply(message.getChat().getId(), "Гость " + guest.getDisplayName() + " добавлен в чек.");
    }

    private TelegramWebhookResult handleAddExpense(TelegramUpdate update, String arguments) {
        TelegramMessage message = update.getMessage();
        TelegramUser from = requiredUser(message);
        AddExpenseArguments parsed = parseAddExpenseArguments(arguments);
        Participant actorParticipant = checkCommandService.requireRegisteredParticipantByInviteToken(
                parsed.inviteToken,
                from.getId().longValue(),
                from.getUsername()
        );
        CheckSnapshot snapshot = checkCommandService.getCheckByInviteToken(parsed.inviteToken);
        List<Participant> splitParticipants = resolveSplitParticipants(snapshot, actorParticipant, parsed.splitParticipantNames);

        ExpenseDetails details = expenseCommandService.createTelegramExpense(
                snapshot.getCheckBook().getId(),
                actorParticipant.getId(),
                parsed.amountMinor,
                parsed.comment,
                message.getText(),
                message.getChat().getId(),
                message.getMessageId(),
                extractParticipantIds(splitParticipants),
                actorParticipant.getId()
        );
        return reply(
                message.getChat().getId(),
                "Расход " + details.getExpense().getAmountMinor() + " RUB добавлен. ID: " + details.getExpense().getId()
                        + ". Делим на: " + joinParticipantNames(splitParticipants) + "."
        );
    }

    private TelegramWebhookResult handleListExpenses(TelegramUpdate update, String arguments) {
        TelegramMessage message = update.getMessage();
        TelegramUser from = requiredUser(message);
        String inviteToken = requireNonBlank(arguments, "Используйте /list_expenses <invite_token>.");
        checkCommandService.requireRegisteredParticipantByInviteToken(inviteToken, from.getId().longValue(), from.getUsername());
        CheckSnapshot snapshot = checkCommandService.getCheckByInviteToken(inviteToken);
        List<ExpenseDetails> expenses = expenseCommandService.listExpenses(snapshot.getCheckBook().getId());
        if (expenses.isEmpty()) {
            return reply(message.getChat().getId(), "В чеке пока нет расходов.");
        }

        StringBuilder builder = new StringBuilder();
        builder.append("Расходы по чеку \"").append(snapshot.getCheckBook().getTitle()).append("\":");
        for (ExpenseDetails expenseDetails : expenses) {
            builder.append("\n")
                    .append(expenseDetails.getExpense().getId())
                    .append(" | ")
                    .append(expenseDetails.getExpense().getAmountMinor())
                    .append(" RUB | ")
                    .append(expenseDetails.getExpense().getStatus().name());
            if (expenseDetails.getExpense().getComment() != null) {
                builder.append(" | ").append(expenseDetails.getExpense().getComment());
            }
        }
        return reply(message.getChat().getId(), builder.toString());
    }

    private TelegramWebhookResult handleUpdateExpense(TelegramUpdate update, String arguments) {
        TelegramMessage message = update.getMessage();
        TelegramUser from = requiredUser(message);
        UpdateExpenseArguments parsed = parseUpdateExpenseArguments(arguments);
        ExpenseDetails currentExpense = expenseCommandService.getExpense(parsed.expenseId);
        Participant actorParticipant = checkCommandService.requireRegisteredParticipant(
                currentExpense.getExpense().getCheckId(),
                from.getId().longValue(),
                from.getUsername()
        );
        CheckSnapshot snapshot = checkCommandService.getCheck(currentExpense.getExpense().getCheckId());
        List<Participant> splitParticipants = resolveSplitParticipants(snapshot, actorParticipant, parsed.splitParticipantNames);

        ExpenseDetails updatedExpense = expenseCommandService.updateExpense(
                parsed.expenseId,
                Long.valueOf(parsed.amountMinor),
                parsed.comment,
                message.getText(),
                extractParticipantIds(splitParticipants),
                null,
                actorParticipant.getId()
        );
        return reply(
                message.getChat().getId(),
                "Расход " + updatedExpense.getExpense().getId() + " обновлен. Новая сумма: "
                        + updatedExpense.getExpense().getAmountMinor() + " RUB. Делим на: "
                        + joinParticipantNames(splitParticipants) + "."
        );
    }

    private TelegramWebhookResult handleDeleteExpense(TelegramUpdate update, String arguments) {
        TelegramMessage message = update.getMessage();
        TelegramUser from = requiredUser(message);
        UUID expenseId = parseExpenseId(arguments);
        ExpenseDetails expenseDetails = expenseCommandService.getExpense(expenseId);
        Participant actorParticipant = checkCommandService.requireRegisteredParticipant(
                expenseDetails.getExpense().getCheckId(),
                from.getId().longValue(),
                from.getUsername()
        );
        expenseCommandService.deleteExpense(expenseId, actorParticipant.getId());
        return reply(message.getChat().getId(), "Расход " + expenseId + " удален.");
    }

    private AddExpenseArguments parseAddExpenseArguments(String arguments) {
        String[] parts = arguments.split("\\|", 2);
        String commandPart = parts[0].trim();
        String comment = parts.length > 1 ? normalizeOptional(parts[1]) : null;

        int firstSpace = commandPart.indexOf(' ');
        int secondSpace = firstSpace < 0 ? -1 : commandPart.indexOf(' ', firstSpace + 1);
        if (firstSpace <= 0 || secondSpace <= firstSpace + 1 || secondSpace == commandPart.length() - 1) {
            throw new ApiException(
                    ApiErrorCode.VALIDATION_ERROR,
                    HttpStatus.BAD_REQUEST,
                    "Используйте /add_expense <invite_token> <amount_minor> <участник1,участник2> | <комментарий>"
            );
        }

        String inviteToken = commandPart.substring(0, firstSpace).trim();
        long amountMinor = parseAmount(commandPart.substring(firstSpace + 1, secondSpace).trim());
        List<String> splitParticipantNames = parseSplitParticipantNames(commandPart.substring(secondSpace + 1).trim());
        return new AddExpenseArguments(inviteToken, amountMinor, splitParticipantNames, comment);
    }

    private UpdateExpenseArguments parseUpdateExpenseArguments(String arguments) {
        String[] parts = arguments.split("\\|", 2);
        String commandPart = parts[0].trim();
        String comment = parts.length > 1 ? normalizeOptional(parts[1]) : null;

        int firstSpace = commandPart.indexOf(' ');
        int secondSpace = firstSpace < 0 ? -1 : commandPart.indexOf(' ', firstSpace + 1);
        if (firstSpace <= 0 || secondSpace <= firstSpace + 1 || secondSpace == commandPart.length() - 1) {
            throw new ApiException(
                    ApiErrorCode.VALIDATION_ERROR,
                    HttpStatus.BAD_REQUEST,
                    "Используйте /update_expense <expense_id> <amount_minor> <участник1,участник2> | <комментарий>"
            );
        }

        UUID expenseId = parseExpenseId(commandPart.substring(0, firstSpace).trim());
        long amountMinor = parseAmount(commandPart.substring(firstSpace + 1, secondSpace).trim());
        List<String> splitParticipantNames = parseSplitParticipantNames(commandPart.substring(secondSpace + 1).trim());
        return new UpdateExpenseArguments(expenseId, amountMinor, splitParticipantNames, comment);
    }

    private long parseAmount(String value) {
        try {
            long amountMinor = Long.parseLong(value);
            if (amountMinor <= 0L) {
                throw new NumberFormatException("amount must be positive");
            }
            return amountMinor;
        } catch (NumberFormatException exception) {
            throw new ApiException(ApiErrorCode.EXPENSE_AMOUNT_INVALID, HttpStatus.BAD_REQUEST, "Сумма расхода должна быть положительным числом в minor units");
        }
    }

    private UUID parseExpenseId(String value) {
        String normalized = value == null ? "" : value.trim();
        if (normalized.isEmpty()) {
            throw new ApiException(ApiErrorCode.VALIDATION_ERROR, HttpStatus.BAD_REQUEST, "Используйте /delete_expense <expense_id>.");
        }
        try {
            return UUID.fromString(normalized);
        } catch (IllegalArgumentException exception) {
            throw new ApiException(ApiErrorCode.VALIDATION_ERROR, HttpStatus.BAD_REQUEST, "expense_id должен быть UUID.");
        }
    }

    private List<String> parseSplitParticipantNames(String rawValue) {
        if (rawValue.isEmpty()) {
            throw new ApiException(ApiErrorCode.EXPENSE_SPLIT_REQUIRED, HttpStatus.BAD_REQUEST, "Нужно указать хотя бы одного участника для деления");
        }

        String[] parts = rawValue.split(",");
        LinkedHashSet<String> uniqueNames = new LinkedHashSet<String>();
        for (String part : parts) {
            String normalized = normalizeParticipantLookup(part);
            if (normalized.isEmpty()) {
                throw new ApiException(ApiErrorCode.VALIDATION_ERROR, HttpStatus.BAD_REQUEST, "Список участников для деления содержит пустое имя");
            }
            uniqueNames.add(normalized);
        }
        if (uniqueNames.isEmpty()) {
            throw new ApiException(ApiErrorCode.EXPENSE_SPLIT_REQUIRED, HttpStatus.BAD_REQUEST, "Нужно указать хотя бы одного участника для деления");
        }
        return new ArrayList<String>(uniqueNames);
    }

    private List<Participant> resolveSplitParticipants(CheckSnapshot snapshot, Participant actorParticipant, List<String> participantNames) {
        List<Participant> result = new ArrayList<Participant>();
        for (String participantName : participantNames) {
            Participant participant = findActiveParticipant(snapshot, actorParticipant, participantName);
            if (participant == null) {
                throw new ApiException(
                        ApiErrorCode.PARTICIPANT_DOES_NOT_BELONG_TO_CHECK,
                        HttpStatus.BAD_REQUEST,
                        "Участник \"" + participantName + "\" не найден в чеке"
                );
            }
            result.add(participant);
        }
        return result;
    }

    private Participant findActiveParticipant(CheckSnapshot snapshot, Participant actorParticipant, String participantName) {
        if ("me".equalsIgnoreCase(participantName)) {
            return actorParticipant;
        }

        String normalizedLookup = normalizeParticipantLookup(participantName);
        for (Participant participant : snapshot.getParticipants()) {
            if (participant.isActive() && normalizeParticipantLookup(participant.getDisplayName()).equalsIgnoreCase(normalizedLookup)) {
                return participant;
            }
        }
        return null;
    }

    private List<UUID> extractParticipantIds(List<Participant> participants) {
        List<UUID> participantIds = new ArrayList<UUID>();
        for (Participant participant : participants) {
            participantIds.add(participant.getId());
        }
        return participantIds;
    }

    private String joinParticipantNames(List<Participant> participants) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < participants.size(); i++) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append(participants.get(i).getDisplayName());
        }
        return builder.toString();
    }

    private String normalizeParticipantLookup(String value) {
        String normalized = value == null ? "" : value.trim();
        while (normalized.startsWith("@")) {
            normalized = normalized.substring(1);
        }
        return normalized.trim();
    }

    private String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String requireNonBlank(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new ApiException(ApiErrorCode.VALIDATION_ERROR, HttpStatus.BAD_REQUEST, message);
        }
        return value.trim();
    }

    private ParsedCommand parseCommand(String text) {
        if (!text.startsWith("/")) {
            return null;
        }

        int firstSpace = text.indexOf(' ');
        String commandToken = firstSpace >= 0 ? text.substring(1, firstSpace) : text.substring(1);
        String arguments = firstSpace >= 0 ? text.substring(firstSpace + 1).trim() : "";

        String commandName = commandToken;
        int mentionSeparator = commandToken.indexOf('@');
        if (mentionSeparator >= 0) {
            commandName = commandToken.substring(0, mentionSeparator);
            String mentionedBot = commandToken.substring(mentionSeparator + 1);
            String currentBot = telegramWebhookProperties.getBotUsername();
            if (currentBot == null || currentBot.trim().isEmpty() || !mentionedBot.equalsIgnoreCase(currentBot.trim())) {
                return ParsedCommand.ignored();
            }
        }

        if (commandName.isEmpty()) {
            return null;
        }
        return ParsedCommand.of(commandName, arguments);
    }

    private TelegramUser requiredUser(TelegramMessage message) {
        if (message.getFrom() == null || message.getFrom().getId() == null) {
            throw new ApiException(ApiErrorCode.VALIDATION_ERROR, HttpStatus.BAD_REQUEST, "В update отсутствуют данные пользователя Telegram");
        }
        return message.getFrom();
    }

    private TelegramWebhookResult emptyResult() {
        return new TelegramWebhookResult(true, Collections.<TelegramOutgoingMessage>emptyList());
    }

    private TelegramWebhookResult reply(Long chatId, String text) {
        return new TelegramWebhookResult(true, Collections.singletonList(new TelegramOutgoingMessage(chatId, text)));
    }

    private static final class AddExpenseArguments {
        private final String inviteToken;
        private final long amountMinor;
        private final List<String> splitParticipantNames;
        private final String comment;

        private AddExpenseArguments(String inviteToken, long amountMinor, List<String> splitParticipantNames, String comment) {
            this.inviteToken = inviteToken;
            this.amountMinor = amountMinor;
            this.splitParticipantNames = splitParticipantNames;
            this.comment = comment;
        }
    }

    private static final class UpdateExpenseArguments {
        private final UUID expenseId;
        private final long amountMinor;
        private final List<String> splitParticipantNames;
        private final String comment;

        private UpdateExpenseArguments(UUID expenseId, long amountMinor, List<String> splitParticipantNames, String comment) {
            this.expenseId = expenseId;
            this.amountMinor = amountMinor;
            this.splitParticipantNames = splitParticipantNames;
            this.comment = comment;
        }
    }

    private static final class ParsedCommand {
        private final String name;
        private final String arguments;
        private final boolean ignored;

        private ParsedCommand(String name, String arguments, boolean ignored) {
            this.name = name;
            this.arguments = arguments;
            this.ignored = ignored;
        }

        private static ParsedCommand of(String name, String arguments) {
            return new ParsedCommand(name, arguments, false);
        }

        private static ParsedCommand ignored() {
            return new ParsedCommand("", "", true);
        }
    }
}

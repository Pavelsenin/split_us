package ru.splitus.telegram;

import java.util.Collections;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ru.splitus.check.CheckCommandService;
import ru.splitus.check.CheckSnapshot;
import ru.splitus.check.Participant;
import ru.splitus.config.TelegramWebhookProperties;
import ru.splitus.error.ApiErrorCode;
import ru.splitus.error.ApiException;

@Service
public class TelegramCommandService {

    private final CheckCommandService checkCommandService;
    private final TelegramWebhookProperties telegramWebhookProperties;

    public TelegramCommandService(CheckCommandService checkCommandService, TelegramWebhookProperties telegramWebhookProperties) {
        this.checkCommandService = checkCommandService;
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
            if (command == null) {
                return emptyResult();
            }
            if (command.ignored) {
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
            return reply(chatId, "Команда не поддерживается. Сейчас доступны /new_check <название>, /start join_<token> и /add_guest <invite_token> <имя>.");
        } catch (ApiException exception) {
            return reply(chatId, exception.getMessage());
        }
    }

    private TelegramWebhookResult handleNewCheck(TelegramUpdate update, String arguments) {
        TelegramMessage message = update.getMessage();
        TelegramUser from = requiredUser(message);
        String payload = arguments;
        if (payload.isEmpty()) {
            return reply(message.getChat().getId(), "Укажите название: /new_check Поход в Питер");
        }

        CheckSnapshot snapshot = checkCommandService.createCheck(payload, from.getId().longValue(), from.getUsername());
        String deepLink = "https://t.me/" + telegramWebhookProperties.getBotUsername() + "?start=join_" + snapshot.getCheckBook().getInviteToken();
        String responseText = "Чек \"" + snapshot.getCheckBook().getTitle() + "\" создан.\n"
                + "Ссылка для присоединения: " + deepLink + "\n"
                + "Fallback для MVP: создайте группу вручную и добавьте туда бота как администратора.";
        return reply(message.getChat().getId(), responseText);
    }

    private TelegramWebhookResult handleStart(TelegramUpdate update, String arguments) {
        TelegramMessage message = update.getMessage();
        TelegramUser from = requiredUser(message);
        String payload = arguments;
        if (!payload.startsWith("join_")) {
            return reply(message.getChat().getId(), "Неизвестный deep link. Ожидался формат /start join_<token>.");
        }

        String inviteToken = payload.substring("join_".length()).trim();
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

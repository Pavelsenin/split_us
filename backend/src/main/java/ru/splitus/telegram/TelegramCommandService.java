package ru.splitus.telegram;

import java.util.Collections;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ru.splitus.check.CheckSnapshot;
import ru.splitus.check.Participant;
import ru.splitus.check.CheckCommandService;
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
            return new TelegramWebhookResult(true, Collections.<TelegramOutgoingMessage>emptyList());
        }

        Long chatId = update.getMessage().getChat().getId();
        String text = update.getMessage().getText();
        if (text == null || text.trim().isEmpty()) {
            return reply(chatId, "Команда не распознана. Используйте /new_check <название> или /start join_<token>.");
        }

        try {
            if (text.startsWith("/new_check")) {
                return handleNewCheck(update);
            }
            if (text.startsWith("/start ")) {
                return handleStart(update);
            }
            return reply(chatId, "Команда не поддерживается. Сейчас доступны /new_check <название> и /start join_<token>.");
        } catch (ApiException exception) {
            return reply(chatId, exception.getMessage());
        }
    }

    private TelegramWebhookResult handleNewCheck(TelegramUpdate update) {
        TelegramMessage message = update.getMessage();
        TelegramUser from = requiredUser(message);
        String payload = message.getText().substring("/new_check".length()).trim();
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

    private TelegramWebhookResult handleStart(TelegramUpdate update) {
        TelegramMessage message = update.getMessage();
        TelegramUser from = requiredUser(message);
        String payload = message.getText().substring("/start".length()).trim();
        if (!payload.startsWith("join_")) {
            return reply(message.getChat().getId(), "Неизвестный deep link. Ожидался формат /start join_<token>.");
        }

        String inviteToken = payload.substring("join_".length()).trim();
        Participant participant = checkCommandService.joinCheckByInviteToken(inviteToken, from.getId().longValue(), from.getUsername());
        return reply(message.getChat().getId(), "Вы присоединились к чеку как @" + participant.getDisplayName() + ".");
    }

    private TelegramUser requiredUser(TelegramMessage message) {
        if (message.getFrom() == null || message.getFrom().getId() == null) {
            throw new ApiException(ApiErrorCode.VALIDATION_ERROR, HttpStatus.BAD_REQUEST, "В update отсутствуют данные пользователя Telegram");
        }
        return message.getFrom();
    }

    private TelegramWebhookResult reply(Long chatId, String text) {
        return new TelegramWebhookResult(true, Collections.singletonList(new TelegramOutgoingMessage(chatId, text)));
    }
}

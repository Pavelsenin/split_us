package ru.splitus.telegram;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TelegramWebhookResult {

    private final boolean accepted;
    private final List<TelegramOutgoingMessage> outgoingMessages;

    public TelegramWebhookResult(boolean accepted, List<TelegramOutgoingMessage> outgoingMessages) {
        this.accepted = accepted;
        this.outgoingMessages = Collections.unmodifiableList(new ArrayList<TelegramOutgoingMessage>(outgoingMessages));
    }

    public boolean isAccepted() {
        return accepted;
    }

    public List<TelegramOutgoingMessage> getOutgoingMessages() {
        return outgoingMessages;
    }
}


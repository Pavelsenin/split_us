package ru.splitus.telegram;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents telegram webhook result.
 */
public class TelegramWebhookResult {

    private final boolean accepted;
    private final List<TelegramOutgoingMessage> outgoingMessages;

    /**
     * Creates a new telegram webhook result instance.
     */
    public TelegramWebhookResult(boolean accepted, List<TelegramOutgoingMessage> outgoingMessages) {
        this.accepted = accepted;
        this.outgoingMessages = Collections.unmodifiableList(new ArrayList<TelegramOutgoingMessage>(outgoingMessages));
    }

    /**
     * Returns whether accepted.
     */
    public boolean isAccepted() {
        return accepted;
    }

    /**
     * Returns the outgoing messages.
     */
    public List<TelegramOutgoingMessage> getOutgoingMessages() {
        return outgoingMessages;
    }
}





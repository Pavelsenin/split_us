package ru.splitus.admin;

import java.util.UUID;

/**
 * Represents admin expense share view.
 */
public class AdminExpenseShareView {

    private final UUID participantId;
    private final String participantDisplayName;
    private final long shareMinor;

    /**
     * Creates a new admin expense share view instance.
     */
    public AdminExpenseShareView(UUID participantId, String participantDisplayName, long shareMinor) {
        this.participantId = participantId;
        this.participantDisplayName = participantDisplayName;
        this.shareMinor = shareMinor;
    }

    /**
     * Returns the participant id.
     */
    public UUID getParticipantId() {
        return participantId;
    }

    /**
     * Returns the participant display name.
     */
    public String getParticipantDisplayName() {
        return participantDisplayName;
    }

    /**
     * Returns the share minor.
     */
    public long getShareMinor() {
        return shareMinor;
    }
}

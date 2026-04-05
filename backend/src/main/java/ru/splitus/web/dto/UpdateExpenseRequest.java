package ru.splitus.web.dto;

import java.util.List;
import java.util.UUID;
import javax.validation.constraints.NotNull;

/**
 * Represents the update expense request payload.
 */
public class UpdateExpenseRequest {

    private Long amountMinor;
    private String comment;
    private String sourceMessageText;
    private List<UUID> splitParticipantIds;
    private String status;

    @NotNull
    private UUID updatedByParticipantId;

    /**
     * Returns the amount minor.
     */
    public Long getAmountMinor() {
        return amountMinor;
    }

    /**
     * Updates the amount minor.
     */
    public void setAmountMinor(Long amountMinor) {
        this.amountMinor = amountMinor;
    }

    /**
     * Returns the comment.
     */
    public String getComment() {
        return comment;
    }

    /**
     * Updates the comment.
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * Returns the source message text.
     */
    public String getSourceMessageText() {
        return sourceMessageText;
    }

    /**
     * Updates the source message text.
     */
    public void setSourceMessageText(String sourceMessageText) {
        this.sourceMessageText = sourceMessageText;
    }

    /**
     * Returns the split participant ids.
     */
    public List<UUID> getSplitParticipantIds() {
        return splitParticipantIds;
    }

    /**
     * Updates the split participant ids.
     */
    public void setSplitParticipantIds(List<UUID> splitParticipantIds) {
        this.splitParticipantIds = splitParticipantIds;
    }

    /**
     * Returns the status.
     */
    public String getStatus() {
        return status;
    }

    /**
     * Updates the status.
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Returns the updated by participant id.
     */
    public UUID getUpdatedByParticipantId() {
        return updatedByParticipantId;
    }

    /**
     * Updates the updated by participant id.
     */
    public void setUpdatedByParticipantId(UUID updatedByParticipantId) {
        this.updatedByParticipantId = updatedByParticipantId;
    }
}





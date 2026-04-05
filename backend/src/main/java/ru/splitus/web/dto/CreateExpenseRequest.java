package ru.splitus.web.dto;

import java.util.List;
import java.util.UUID;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * Represents the create expense request payload.
 */
public class CreateExpenseRequest {

    @NotNull
    private UUID payerParticipantId;

    @NotNull
    private Long amountMinor;

    private String comment;

    private String sourceMessageText;

    @NotEmpty
    private List<UUID> splitParticipantIds;

    @NotNull
    private UUID createdByParticipantId;

    /**
     * Returns the payer participant id.
     */
    public UUID getPayerParticipantId() {
        return payerParticipantId;
    }

    /**
     * Updates the payer participant id.
     */
    public void setPayerParticipantId(UUID payerParticipantId) {
        this.payerParticipantId = payerParticipantId;
    }

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
     * Returns the created by participant id.
     */
    public UUID getCreatedByParticipantId() {
        return createdByParticipantId;
    }

    /**
     * Updates the created by participant id.
     */
    public void setCreatedByParticipantId(UUID createdByParticipantId) {
        this.createdByParticipantId = createdByParticipantId;
    }
}





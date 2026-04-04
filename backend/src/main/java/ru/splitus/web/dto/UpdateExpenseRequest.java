package ru.splitus.web.dto;

import java.util.List;
import java.util.UUID;
import javax.validation.constraints.NotNull;

public class UpdateExpenseRequest {

    private Long amountMinor;
    private String comment;
    private String sourceMessageText;
    private List<UUID> splitParticipantIds;
    private String status;

    @NotNull
    private UUID updatedByParticipantId;

    public Long getAmountMinor() {
        return amountMinor;
    }

    public void setAmountMinor(Long amountMinor) {
        this.amountMinor = amountMinor;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getSourceMessageText() {
        return sourceMessageText;
    }

    public void setSourceMessageText(String sourceMessageText) {
        this.sourceMessageText = sourceMessageText;
    }

    public List<UUID> getSplitParticipantIds() {
        return splitParticipantIds;
    }

    public void setSplitParticipantIds(List<UUID> splitParticipantIds) {
        this.splitParticipantIds = splitParticipantIds;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public UUID getUpdatedByParticipantId() {
        return updatedByParticipantId;
    }

    public void setUpdatedByParticipantId(UUID updatedByParticipantId) {
        this.updatedByParticipantId = updatedByParticipantId;
    }
}


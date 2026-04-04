package ru.splitus.web.dto;

import java.util.List;
import java.util.UUID;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

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

    public UUID getPayerParticipantId() {
        return payerParticipantId;
    }

    public void setPayerParticipantId(UUID payerParticipantId) {
        this.payerParticipantId = payerParticipantId;
    }

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

    public UUID getCreatedByParticipantId() {
        return createdByParticipantId;
    }

    public void setCreatedByParticipantId(UUID createdByParticipantId) {
        this.createdByParticipantId = createdByParticipantId;
    }
}


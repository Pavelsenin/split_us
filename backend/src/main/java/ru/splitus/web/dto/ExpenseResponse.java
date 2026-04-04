package ru.splitus.web.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import ru.splitus.expense.ExpenseDetails;

public class ExpenseResponse {

    private UUID id;
    private UUID checkId;
    private long amountMinor;
    private String currencyCode;
    private UUID payerParticipantId;
    private String comment;
    private String sourceMessageText;
    private String status;
    private UUID createdByParticipantId;
    private UUID updatedByParticipantId;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private List<ExpenseShareResponse> shares;

    public static ExpenseResponse fromDomain(ExpenseDetails details) {
        ExpenseResponse response = new ExpenseResponse();
        response.id = details.getExpense().getId();
        response.checkId = details.getExpense().getCheckId();
        response.amountMinor = details.getExpense().getAmountMinor();
        response.currencyCode = details.getExpense().getCurrencyCode();
        response.payerParticipantId = details.getExpense().getPayerParticipantId();
        response.comment = details.getExpense().getComment();
        response.sourceMessageText = details.getExpense().getSourceMessageText();
        response.status = details.getExpense().getStatus().name();
        response.createdByParticipantId = details.getExpense().getCreatedByParticipantId();
        response.updatedByParticipantId = details.getExpense().getUpdatedByParticipantId();
        response.createdAt = details.getExpense().getCreatedAt();
        response.updatedAt = details.getExpense().getUpdatedAt();
        response.shares = details.getShares().stream().map(ExpenseShareResponse::fromDomain).collect(Collectors.toList());
        return response;
    }

    public UUID getId() {
        return id;
    }

    public UUID getCheckId() {
        return checkId;
    }

    public long getAmountMinor() {
        return amountMinor;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public UUID getPayerParticipantId() {
        return payerParticipantId;
    }

    public String getComment() {
        return comment;
    }

    public String getSourceMessageText() {
        return sourceMessageText;
    }

    public String getStatus() {
        return status;
    }

    public UUID getCreatedByParticipantId() {
        return createdByParticipantId;
    }

    public UUID getUpdatedByParticipantId() {
        return updatedByParticipantId;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public List<ExpenseShareResponse> getShares() {
        return shares;
    }
}


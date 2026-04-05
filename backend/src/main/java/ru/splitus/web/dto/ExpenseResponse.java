package ru.splitus.web.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import ru.splitus.expense.ExpenseDetails;

/**
 * Represents the expense response payload.
 */
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

    /**
     * Creates expense response from domain.
     */
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

    /**
     * Returns the id.
     */
    public UUID getId() {
        return id;
    }

    /**
     * Returns the check id.
     */
    public UUID getCheckId() {
        return checkId;
    }

    /**
     * Returns the amount minor.
     */
    public long getAmountMinor() {
        return amountMinor;
    }

    /**
     * Returns the currency code.
     */
    public String getCurrencyCode() {
        return currencyCode;
    }

    /**
     * Returns the payer participant id.
     */
    public UUID getPayerParticipantId() {
        return payerParticipantId;
    }

    /**
     * Returns the comment.
     */
    public String getComment() {
        return comment;
    }

    /**
     * Returns the source message text.
     */
    public String getSourceMessageText() {
        return sourceMessageText;
    }

    /**
     * Returns the status.
     */
    public String getStatus() {
        return status;
    }

    /**
     * Returns the created by participant id.
     */
    public UUID getCreatedByParticipantId() {
        return createdByParticipantId;
    }

    /**
     * Returns the updated by participant id.
     */
    public UUID getUpdatedByParticipantId() {
        return updatedByParticipantId;
    }

    /**
     * Returns the created at.
     */
    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Returns the updated at.
     */
    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Returns the shares.
     */
    public List<ExpenseShareResponse> getShares() {
        return shares;
    }
}





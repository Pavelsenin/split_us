package ru.splitus.expense;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.splitus.check.CheckBook;
import ru.splitus.check.CheckBookRepository;
import ru.splitus.check.Participant;
import ru.splitus.check.ParticipantRepository;
import ru.splitus.error.ApiErrorCode;
import ru.splitus.error.ApiException;

/**
 * Coordinates expense command operations.
 */
@Service
public class ExpenseCommandService {

    private static final String RUB = "RUB";

    private final CheckBookRepository checkBookRepository;
    private final ParticipantRepository participantRepository;
    private final ExpenseRepository expenseRepository;
    private final ExpenseShareRepository expenseShareRepository;

    /**
     * Creates a new expense command service instance.
     */
    public ExpenseCommandService(
            CheckBookRepository checkBookRepository,
            ParticipantRepository participantRepository,
            ExpenseRepository expenseRepository,
            ExpenseShareRepository expenseShareRepository) {
        this.checkBookRepository = checkBookRepository;
        this.participantRepository = participantRepository;
        this.expenseRepository = expenseRepository;
        this.expenseShareRepository = expenseShareRepository;
    }

    /**
     * Creates expense.
     */
    @Transactional
    public ExpenseDetails createExpense(
            UUID checkId,
            UUID payerParticipantId,
            long amountMinor,
            String comment,
            String sourceMessageText,
            List<UUID> splitParticipantIds,
            UUID actorParticipantId) {
        return createExpenseInternal(
                checkId,
                payerParticipantId,
                amountMinor,
                comment,
                sourceMessageText,
                null,
                null,
                splitParticipantIds,
                actorParticipantId
        );
    }

    /**
     * Creates telegram expense.
     */
    @Transactional
    public ExpenseDetails createTelegramExpense(
            UUID checkId,
            UUID payerParticipantId,
            long amountMinor,
            String comment,
            String sourceMessageText,
            Long telegramChatId,
            Long telegramMessageId,
            List<UUID> splitParticipantIds,
            UUID actorParticipantId) {
        return createExpenseInternal(
                checkId,
                payerParticipantId,
                amountMinor,
                comment,
                sourceMessageText,
                telegramChatId,
                telegramMessageId,
                splitParticipantIds,
                actorParticipantId
        );
    }

    private ExpenseDetails createExpenseInternal(
            UUID checkId,
            UUID payerParticipantId,
            long amountMinor,
            String comment,
            String sourceMessageText,
            Long telegramChatId,
            Long telegramMessageId,
            List<UUID> splitParticipantIds,
            UUID actorParticipantId) {
        CheckBook checkBook = loadCheck(checkId);
        Map<UUID, Participant> activeParticipants = activeParticipantMap(checkId);
        ensureParticipantInCheck(activeParticipants, actorParticipantId);
        ensureParticipantInCheck(activeParticipants, payerParticipantId);

        List<UUID> normalizedSplitIds = normalizeSplitParticipantIds(splitParticipantIds);
        List<Participant> splitParticipants = resolveSplitParticipants(activeParticipants, normalizedSplitIds);
        validateAmount(amountMinor);

        OffsetDateTime now = OffsetDateTime.now();
        Expense expense = new Expense(
                UUID.randomUUID(),
                checkBook.getId(),
                amountMinor,
                RUB,
                payerParticipantId,
                normalizeOptional(comment),
                normalizeOptional(sourceMessageText),
                telegramChatId,
                telegramMessageId,
                ExpenseStatus.VALID,
                actorParticipantId,
                actorParticipantId,
                now,
                now
        );
        List<ExpenseShare> shares = buildShares(expense.getId(), amountMinor, splitParticipants);
        expenseRepository.save(expense);
        expenseShareRepository.saveAll(shares);
        return new ExpenseDetails(expense, shares);
    }

    /**
     * Returns the expense.
     */
    @Transactional(readOnly = true)
    public ExpenseDetails getExpense(UUID expenseId) {
        Expense expense = loadExpense(expenseId);
        return new ExpenseDetails(expense, expenseShareRepository.findByExpenseId(expenseId));
    }

    /**
     * Finds expense by telegram message.
     */
    @Transactional(readOnly = true)
    public Optional<ExpenseDetails> findExpenseByTelegramMessage(long telegramChatId, long telegramMessageId) {
        Optional<Expense> expense = expenseRepository.findByTelegramMessage(telegramChatId, telegramMessageId);
        if (!expense.isPresent()) {
            return Optional.empty();
        }
        return Optional.of(new ExpenseDetails(expense.get(), expenseShareRepository.findByExpenseId(expense.get().getId())));
    }

    /**
     * Lists expenses.
     */
    @Transactional(readOnly = true)
    public List<ExpenseDetails> listExpenses(UUID checkId) {
        loadCheck(checkId);
        List<Expense> expenses = expenseRepository.findByCheckId(checkId);
        List<ExpenseDetails> details = new ArrayList<ExpenseDetails>();
        for (Expense expense : expenses) {
            details.add(new ExpenseDetails(expense, expenseShareRepository.findByExpenseId(expense.getId())));
        }
        return details;
    }

    /**
     * Updates expense.
     */
    @Transactional
    public ExpenseDetails updateExpense(
            UUID expenseId,
            Long amountMinor,
            String comment,
            String sourceMessageText,
            List<UUID> splitParticipantIds,
            ExpenseStatus status,
            UUID actorParticipantId) {

        Expense existingExpense = loadExpense(expenseId);
        Map<UUID, Participant> activeParticipants = activeParticipantMap(existingExpense.getCheckId());
        ensureParticipantInCheck(activeParticipants, actorParticipantId);
        ensureAuthor(existingExpense, actorParticipantId);

        long resolvedAmount = amountMinor == null ? existingExpense.getAmountMinor() : amountMinor.longValue();
        validateAmount(resolvedAmount);

        ExpenseStatus resolvedStatus = status == null ? existingExpense.getStatus() : status;
        String resolvedComment = comment == null ? existingExpense.getComment() : normalizeOptional(comment);
        String resolvedSourceMessageText = sourceMessageText == null ? existingExpense.getSourceMessageText() : normalizeOptional(sourceMessageText);

        List<ExpenseShare> currentShares = expenseShareRepository.findByExpenseId(expenseId);
        List<UUID> resolvedSplitIds = splitParticipantIds == null
                ? extractParticipantIds(currentShares)
                : normalizeSplitParticipantIds(splitParticipantIds);
        List<Participant> splitParticipants = resolveSplitParticipants(activeParticipants, resolvedSplitIds);

        Expense updatedExpense = new Expense(
                existingExpense.getId(),
                existingExpense.getCheckId(),
                resolvedAmount,
                existingExpense.getCurrencyCode(),
                existingExpense.getPayerParticipantId(),
                resolvedComment,
                resolvedSourceMessageText,
                existingExpense.getTelegramChatId(),
                existingExpense.getTelegramMessageId(),
                resolvedStatus,
                existingExpense.getCreatedByParticipantId(),
                actorParticipantId,
                existingExpense.getCreatedAt(),
                OffsetDateTime.now()
        );
        List<ExpenseShare> updatedShares = buildShares(updatedExpense.getId(), resolvedAmount, splitParticipants);

        expenseRepository.update(updatedExpense);
        expenseShareRepository.deleteByExpenseId(updatedExpense.getId());
        expenseShareRepository.saveAll(updatedShares);
        return new ExpenseDetails(updatedExpense, updatedShares);
    }

    /**
     * Deletes expense.
     */
    @Transactional
    public void deleteExpense(UUID expenseId, UUID actorParticipantId) {
        Expense existingExpense = loadExpense(expenseId);
        Map<UUID, Participant> activeParticipants = activeParticipantMap(existingExpense.getCheckId());
        ensureParticipantInCheck(activeParticipants, actorParticipantId);
        ensureAuthor(existingExpense, actorParticipantId);
        expenseShareRepository.deleteByExpenseId(expenseId);
        expenseRepository.deleteById(expenseId);
    }

    private CheckBook loadCheck(UUID checkId) {
        return checkBookRepository.findById(checkId)
                .orElseThrow(() -> new ApiException(ApiErrorCode.CHECK_NOT_FOUND, HttpStatus.NOT_FOUND, "Check not found"));
    }

    private Expense loadExpense(UUID expenseId) {
        return expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ApiException(ApiErrorCode.EXPENSE_NOT_FOUND, HttpStatus.NOT_FOUND, "Expense not found"));
    }

    private Map<UUID, Participant> activeParticipantMap(UUID checkId) {
        List<Participant> participants = participantRepository.findByCheckId(checkId);
        Map<UUID, Participant> result = new HashMap<UUID, Participant>();
        for (Participant participant : participants) {
            if (participant.isActive()) {
                result.put(participant.getId(), participant);
            }
        }
        return result;
    }

    private void ensureParticipantInCheck(Map<UUID, Participant> activeParticipants, UUID participantId) {
        if (!activeParticipants.containsKey(participantId)) {
            throw new ApiException(
                    ApiErrorCode.PARTICIPANT_DOES_NOT_BELONG_TO_CHECK,
                    HttpStatus.BAD_REQUEST,
                    "Participant does not belong to check"
            );
        }
    }

    private void ensureAuthor(Expense expense, UUID actorParticipantId) {
        if (!expense.getCreatedByParticipantId().equals(actorParticipantId)) {
            throw new ApiException(
                    ApiErrorCode.EXPENSE_AUTHOR_MISMATCH,
                    HttpStatus.FORBIDDEN,
                    "Only the expense author can change this expense"
            );
        }
    }

    private void validateAmount(long amountMinor) {
        if (amountMinor <= 0L) {
            throw new ApiException(
                    ApiErrorCode.EXPENSE_AMOUNT_INVALID,
                    HttpStatus.BAD_REQUEST,
                    "Expense amount must be positive"
            );
        }
    }

    private List<UUID> normalizeSplitParticipantIds(List<UUID> splitParticipantIds) {
        if (splitParticipantIds == null || splitParticipantIds.isEmpty()) {
            throw new ApiException(
                    ApiErrorCode.EXPENSE_SPLIT_REQUIRED,
                    HttpStatus.BAD_REQUEST,
                    "At least one participant must be selected for split"
            );
        }

        LinkedHashSet<UUID> uniqueIds = new LinkedHashSet<UUID>(splitParticipantIds);
        if (uniqueIds.size() != splitParticipantIds.size()) {
            throw new ApiException(
                    ApiErrorCode.VALIDATION_ERROR,
                    HttpStatus.BAD_REQUEST,
                    "Split participant ids must be unique"
            );
        }
        return new ArrayList<UUID>(uniqueIds);
    }

    private List<Participant> resolveSplitParticipants(Map<UUID, Participant> activeParticipants, List<UUID> participantIds) {
        List<Participant> splitParticipants = new ArrayList<Participant>();
        for (UUID participantId : participantIds) {
            Participant participant = activeParticipants.get(participantId);
            if (participant == null) {
                throw new ApiException(
                        ApiErrorCode.PARTICIPANT_DOES_NOT_BELONG_TO_CHECK,
                        HttpStatus.BAD_REQUEST,
                        "Split participant does not belong to check"
                );
            }
            splitParticipants.add(participant);
        }

        splitParticipants.sort((left, right) -> left.getCreatedAt().compareTo(right.getCreatedAt()));
        return splitParticipants;
    }

    private List<ExpenseShare> buildShares(UUID expenseId, long amountMinor, List<Participant> splitParticipants) {
        long baseShare = amountMinor / splitParticipants.size();
        long remainder = amountMinor % splitParticipants.size();
        List<ExpenseShare> shares = new ArrayList<ExpenseShare>();

        for (int i = 0; i < splitParticipants.size(); i++) {
            long shareMinor = baseShare;
            if (i == 0) {
                shareMinor += remainder;
            }
            shares.add(new ExpenseShare(expenseId, splitParticipants.get(i).getId(), shareMinor));
        }
        return shares;
    }

    private List<UUID> extractParticipantIds(List<ExpenseShare> shares) {
        List<UUID> participantIds = new ArrayList<UUID>();
        for (ExpenseShare share : shares) {
            participantIds.add(share.getParticipantId());
        }
        return participantIds;
    }

    private String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}




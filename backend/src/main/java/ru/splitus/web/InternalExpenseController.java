package ru.splitus.web;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.splitus.error.ApiErrorCode;
import ru.splitus.error.ApiException;
import ru.splitus.expense.ExpenseCommandService;
import ru.splitus.expense.ExpenseDetails;
import ru.splitus.expense.ExpenseStatus;
import ru.splitus.web.dto.CreateExpenseRequest;
import ru.splitus.web.dto.ExpenseResponse;
import ru.splitus.web.dto.UpdateExpenseRequest;

@RestController
@RequestMapping("/api/internal")
public class InternalExpenseController {

    private final ExpenseCommandService expenseCommandService;

    public InternalExpenseController(ExpenseCommandService expenseCommandService) {
        this.expenseCommandService = expenseCommandService;
    }

    @PostMapping("/checks/{checkId}/expenses")
    public ResponseEntity<ExpenseResponse> createExpense(
            @PathVariable UUID checkId,
            @Valid @RequestBody CreateExpenseRequest request) {
        ExpenseDetails details = expenseCommandService.createExpense(
                checkId,
                request.getPayerParticipantId(),
                request.getAmountMinor().longValue(),
                request.getComment(),
                request.getSourceMessageText(),
                request.getSplitParticipantIds(),
                request.getCreatedByParticipantId()
        );
        return ResponseEntity.created(URI.create("/api/internal/expenses/" + details.getExpense().getId()))
                .body(ExpenseResponse.fromDomain(details));
    }

    @GetMapping("/checks/{checkId}/expenses")
    public List<ExpenseResponse> listExpenses(@PathVariable UUID checkId) {
        return expenseCommandService.listExpenses(checkId)
                .stream()
                .map(ExpenseResponse::fromDomain)
                .collect(Collectors.toList());
    }

    @GetMapping("/expenses/{expenseId}")
    public ExpenseResponse getExpense(@PathVariable UUID expenseId) {
        return ExpenseResponse.fromDomain(expenseCommandService.getExpense(expenseId));
    }

    @PatchMapping("/expenses/{expenseId}")
    public ExpenseResponse updateExpense(
            @PathVariable UUID expenseId,
            @Valid @RequestBody UpdateExpenseRequest request) {
        return ExpenseResponse.fromDomain(expenseCommandService.updateExpense(
                expenseId,
                request.getAmountMinor(),
                request.getComment(),
                request.getSourceMessageText(),
                request.getSplitParticipantIds(),
                parseStatus(request.getStatus()),
                request.getUpdatedByParticipantId()
        ));
    }

    @DeleteMapping("/expenses/{expenseId}")
    public ResponseEntity<Void> deleteExpense(
            @PathVariable UUID expenseId,
            @RequestParam UUID actorParticipantId) {
        expenseCommandService.deleteExpense(expenseId, actorParticipantId);
        return ResponseEntity.noContent().build();
    }

    private ExpenseStatus parseStatus(String status) {
        if (status == null) {
            return null;
        }
        try {
            return ExpenseStatus.valueOf(status);
        } catch (IllegalArgumentException exception) {
            throw new ApiException(ApiErrorCode.VALIDATION_ERROR, org.springframework.http.HttpStatus.BAD_REQUEST, "Unknown expense status");
        }
    }
}

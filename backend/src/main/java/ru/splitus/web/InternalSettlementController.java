package ru.splitus.web;

import java.util.UUID;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.splitus.settlement.SettlementExecutionService;
import ru.splitus.web.dto.SettlementResponse;

/**
 * Internal endpoint for executing settlement calculation for a specific check.
 */
@RestController
@RequestMapping("/api/internal/checks")
public class InternalSettlementController {

    private final SettlementExecutionService settlementExecutionService;

    /**
     * Creates a new internal settlement controller instance.
     */
    public InternalSettlementController(SettlementExecutionService settlementExecutionService) {
        this.settlementExecutionService = settlementExecutionService;
    }

    /**
     * Calculates the current settlement plan for the provided check.
     *
     * @param checkId target check identifier
     * @return settlement balances and transfer plan
     */
    @PostMapping("/{checkId}/settlement")
    public SettlementResponse calculateSettlement(@PathVariable UUID checkId) {
        return SettlementResponse.fromDomain(settlementExecutionService.calculateStable(checkId));
    }
}




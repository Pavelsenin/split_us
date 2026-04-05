package ru.splitus.web;

import java.util.UUID;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.splitus.settlement.SettlementExecutionService;
import ru.splitus.web.dto.SettlementResponse;

@RestController
@RequestMapping("/api/internal/checks")
public class InternalSettlementController {

    private final SettlementExecutionService settlementExecutionService;

    public InternalSettlementController(SettlementExecutionService settlementExecutionService) {
        this.settlementExecutionService = settlementExecutionService;
    }

    @PostMapping("/{checkId}/settlement")
    public SettlementResponse calculateSettlement(@PathVariable UUID checkId) {
        return SettlementResponse.fromDomain(settlementExecutionService.calculateStable(checkId));
    }
}

package ru.splitus.web;

import java.util.UUID;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.splitus.settlement.SettlementQueryService;
import ru.splitus.web.dto.SettlementResponse;

@RestController
@RequestMapping("/api/internal/checks")
public class InternalSettlementController {

    private final SettlementQueryService settlementQueryService;

    public InternalSettlementController(SettlementQueryService settlementQueryService) {
        this.settlementQueryService = settlementQueryService;
    }

    @PostMapping("/{checkId}/settlement")
    public SettlementResponse calculateSettlement(@PathVariable UUID checkId) {
        return SettlementResponse.fromDomain(settlementQueryService.calculate(checkId));
    }
}

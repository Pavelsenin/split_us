package ru.splitus.web.dto;

import java.util.List;
import java.util.stream.Collectors;
import ru.splitus.settlement.SettlementResult;

/**
 * API response DTO for a full settlement calculation result.
 */
public class SettlementResponse {

    private int transferCount;
    private List<SettlementBalanceResponse> balances;
    private List<SettlementTransferResponse> transfers;

    /**
     * Maps the settlement-domain result to the API response shape.
     *
     * @param result domain settlement result
     * @return response DTO
     */
    public static SettlementResponse fromDomain(SettlementResult result) {
        SettlementResponse response = new SettlementResponse();
        response.transferCount = result.getPlan().size();
        response.balances = result.getBalances().stream()
                .map(SettlementBalanceResponse::fromDomain)
                .collect(Collectors.toList());
        response.transfers = result.getPlan().getTransfers().stream()
                .map(SettlementTransferResponse::fromDomain)
                .collect(Collectors.toList());
        return response;
    }

    /**
     * @return number of transfers in the settlement plan
     */
    public int getTransferCount() {
        return transferCount;
    }

    /**
     * @return participant balances included in the result
     */
    public List<SettlementBalanceResponse> getBalances() {
        return balances;
    }

    /**
     * @return settlement transfer instructions
     */
    public List<SettlementTransferResponse> getTransfers() {
        return transfers;
    }
}



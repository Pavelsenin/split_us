package ru.splitus.web.dto;

import java.util.List;
import java.util.stream.Collectors;
import ru.splitus.settlement.SettlementResult;

public class SettlementResponse {

    private int transferCount;
    private List<SettlementBalanceResponse> balances;
    private List<SettlementTransferResponse> transfers;

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

    public int getTransferCount() {
        return transferCount;
    }

    public List<SettlementBalanceResponse> getBalances() {
        return balances;
    }

    public List<SettlementTransferResponse> getTransfers() {
        return transfers;
    }
}

package com.minhthien.hoser_backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class AdminPayoutDebtSummaryResponse {
    private BigDecimal totalAmount;
    private Integer debtCount;
    private List<AdminPayoutDebtResponse> debts;
}

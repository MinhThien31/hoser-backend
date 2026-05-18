package com.minhthien.hoser_backend.dto.response;

import com.minhthien.hoser_backend.enums.AdminWalletWithdrawalStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminWalletWithdrawalResponse {
    private Long id;
    private Long adminId;
    private BigDecimal amount;
    private String currency;
    private AdminWalletWithdrawalStatus status;
    private String bankName;
    private String bankAccountNumber;
    private String bankAccountName;
    private String reason;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
}

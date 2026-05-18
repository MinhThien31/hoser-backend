package com.minhthien.hoser_backend.dto.response;

import com.minhthien.hoser_backend.enums.WithdrawalStatus;
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
public class WithdrawalResponse {
    private Long id;
    private Long userId;
    private BigDecimal amount;
    private String currency;
    private WithdrawalStatus status;
    private String bankName;
    private String bankAccountNumber;
    private String bankAccountName;
    private String reason;
    private String adminNote;
    private Long approvedBy;
    private Long rejectedBy;
    private Long paidBy;
    private LocalDateTime approvedAt;
    private LocalDateTime rejectedAt;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

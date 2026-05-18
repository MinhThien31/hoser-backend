package com.minhthien.hoser_backend.dto.response;

import com.minhthien.hoser_backend.enums.WalletTransactionDirection;
import com.minhthien.hoser_backend.enums.WalletTransactionStatus;
import com.minhthien.hoser_backend.enums.WalletTransactionType;
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
public class WalletTransactionResponse {
    private Long id;
    private Long walletId;
    private Long userId;
    private WalletTransactionType type;
    private WalletTransactionDirection direction;
    private BigDecimal amount;
    private BigDecimal availableBefore;
    private BigDecimal availableAfter;
    private BigDecimal holdBefore;
    private BigDecimal holdAfter;
    private WalletTransactionStatus status;
    private String referenceType;
    private String referenceId;
    private String idempotencyKey;
    private String metadata;
    private String note;
    private LocalDateTime createdAt;
}

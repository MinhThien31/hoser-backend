package com.minhthien.hoser_backend.dto.response;

import com.minhthien.hoser_backend.enums.WalletOwnerType;
import com.minhthien.hoser_backend.enums.WalletStatus;
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
public class WalletResponse {
    private Long id;
    private WalletOwnerType ownerType;
    private Long userId;
    private String currency;
    private BigDecimal availableBalance;
    private BigDecimal holdBalance;
    private BigDecimal totalBalance;
    private WalletStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

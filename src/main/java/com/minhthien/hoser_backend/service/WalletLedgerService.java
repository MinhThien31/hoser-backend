package com.minhthien.hoser_backend.service;

import com.minhthien.hoser_backend.entity.Wallet;
import com.minhthien.hoser_backend.entity.WalletTransaction;
import com.minhthien.hoser_backend.enums.WalletTransactionDirection;
import com.minhthien.hoser_backend.enums.WalletTransactionType;

import java.math.BigDecimal;

public interface WalletLedgerService {
    WalletTransaction record(
            Wallet wallet,
            WalletTransactionType type,
            WalletTransactionDirection direction,
            BigDecimal amount,
            BigDecimal availableBefore,
            BigDecimal availableAfter,
            BigDecimal holdBefore,
            BigDecimal holdAfter,
            String referenceType,
            String referenceId,
            String idempotencyKey,
            String metadata,
            String note
    );
}

package com.minhthien.hoser_backend.service.impl;

import com.minhthien.hoser_backend.entity.Wallet;
import com.minhthien.hoser_backend.entity.WalletTransaction;
import com.minhthien.hoser_backend.enums.WalletTransactionDirection;
import com.minhthien.hoser_backend.enums.WalletTransactionStatus;
import com.minhthien.hoser_backend.enums.WalletTransactionType;
import com.minhthien.hoser_backend.repository.WalletTransactionRepository;
import com.minhthien.hoser_backend.service.WalletLedgerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class WalletLedgerServiceImpl implements WalletLedgerService {

    private final WalletTransactionRepository walletTransactionRepository;

    @Override
    public WalletTransaction record(
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
    ) {
        WalletTransaction transaction = WalletTransaction.builder()
                .wallet(wallet)
                .user(wallet.getUser())
                .type(type)
                .direction(direction)
                .amount(amount)
                .availableBefore(availableBefore)
                .availableAfter(availableAfter)
                .holdBefore(holdBefore)
                .holdAfter(holdAfter)
                .status(WalletTransactionStatus.SUCCESS)
                .referenceType(referenceType)
                .referenceId(referenceId)
                .idempotencyKey(idempotencyKey)
                .metadata(metadata)
                .note(note)
                .build();

        return walletTransactionRepository.save(transaction);
    }
}

package com.minhthien.hoser_backend.entity;

import com.minhthien.hoser_backend.enums.WalletTransactionDirection;
import com.minhthien.hoser_backend.enums.WalletTransactionStatus;
import com.minhthien.hoser_backend.enums.WalletTransactionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "wallet_transactions",
        indexes = {
                @Index(name = "idx_wallet_transactions_wallet", columnList = "wallet_id"),
                @Index(name = "idx_wallet_transactions_user", columnList = "user_id"),
                @Index(name = "idx_wallet_transactions_reference", columnList = "reference_type, reference_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_wallet_transactions_idempotency", columnNames = "idempotency_key")
        }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private WalletTransactionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private WalletTransactionDirection direction;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal availableBefore;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal availableAfter;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal holdBefore;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal holdAfter;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private WalletTransactionStatus status = WalletTransactionStatus.SUCCESS;

    @Column(name = "reference_type", length = 100)
    private String referenceType;

    @Column(name = "reference_id", length = 100)
    private String referenceId;

    @Column(name = "idempotency_key", length = 150)
    private String idempotencyKey;

    @Column(columnDefinition = "TEXT")
    private String metadata;

    @Column(length = 500)
    private String note;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(length = 100)
    @Builder.Default
    private String createdBy = "SYSTEM";

    @Column(length = 100)
    @Builder.Default
    private String updatedBy = "SYSTEM";

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
        if (status == null) {
            status = WalletTransactionStatus.SUCCESS;
        }
        if (createdBy == null || createdBy.isBlank()) {
            createdBy = "SYSTEM";
        }
        if (updatedBy == null || updatedBy.isBlank()) {
            updatedBy = createdBy;
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
        if (updatedBy == null || updatedBy.isBlank()) {
            updatedBy = "SYSTEM";
        }
    }
}

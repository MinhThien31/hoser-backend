package com.minhthien.hoser_backend.entity;

import com.minhthien.hoser_backend.enums.WithdrawalStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "withdrawal_requests",
        indexes = {
                @Index(name = "idx_withdrawal_requests_user", columnList = "user_id"),
                @Index(name = "idx_withdrawal_requests_status", columnList = "status")
        }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WithdrawalRequest {
    public static final String DEFAULT_CURRENCY = "VND";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 10)
    @Builder.Default
    private String currency = DEFAULT_CURRENCY;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private WithdrawalStatus status = WithdrawalStatus.PENDING;

    @Column(nullable = false, length = 120)
    private String bankName;

    @Column(nullable = false, length = 80)
    private String bankAccountNumber;

    @Column(nullable = false, length = 120)
    private String bankAccountName;

    @Column(length = 500)
    private String reason;

    @Column(length = 500)
    private String adminNote;

    private Long approvedBy;

    private Long rejectedBy;

    private Long paidBy;

    private LocalDateTime approvedAt;

    private LocalDateTime rejectedAt;

    private LocalDateTime paidAt;

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
        if (currency == null || currency.isBlank()) {
            currency = DEFAULT_CURRENCY;
        }
        if (status == null) {
            status = WithdrawalStatus.PENDING;
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

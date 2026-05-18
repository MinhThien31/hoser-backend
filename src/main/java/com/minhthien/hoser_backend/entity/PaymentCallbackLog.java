package com.minhthien.hoser_backend.entity;

import com.minhthien.hoser_backend.enums.PaymentOrderStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "payment_callback_logs",
        indexes = {
                @Index(name = "idx_payment_callback_logs_reference", columnList = "reference_code"),
                @Index(name = "idx_payment_callback_logs_processed_at", columnList = "processed_at")
        }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCallbackLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reference_code", length = 80)
    private String referenceCode;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private PaymentOrderStatus status;

    @Column(name = "provider_transaction_id", length = 150)
    private String providerTransactionId;

    @Column(nullable = false)
    private boolean tokenValid;

    @Column(nullable = false)
    private boolean processed;

    @Column(length = 500)
    private String errorMessage;

    @Column(columnDefinition = "TEXT")
    private String metadata;

    @Column(name = "processed_at", nullable = false)
    @Builder.Default
    private LocalDateTime processedAt = LocalDateTime.now();

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (processedAt == null) {
            processedAt = now;
        }
        if (createdAt == null) {
            createdAt = now;
        }
    }
}

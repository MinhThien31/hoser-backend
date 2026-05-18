package com.minhthien.hoser_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "admin_audit_logs",
        indexes = {
                @Index(name = "idx_admin_audit_logs_admin", columnList = "admin_id"),
                @Index(name = "idx_admin_audit_logs_reference", columnList = "reference_type, reference_id")
        }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminAuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "admin_id", nullable = false)
    private Long adminId;

    @Column(nullable = false, length = 100)
    private String action;

    @Column(length = 100)
    private String referenceType;

    @Column(length = 100)
    private String referenceId;

    @Column(precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(length = 500)
    private String reason;

    @Column(columnDefinition = "TEXT")
    private String metadata;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}

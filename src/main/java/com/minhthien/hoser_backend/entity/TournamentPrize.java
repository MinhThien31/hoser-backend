package com.minhthien.hoser_backend.entity;

import com.minhthien.hoser_backend.enums.PrizeRecipientPolicy;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "tournament_prizes",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_tournament_prize_rank", columnNames = {"tournament_id", "prize_rank"})
        },
        indexes = {
                @Index(name = "idx_tournament_prizes_tournament", columnList = "tournament_id")
        }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TournamentPrize {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tournament_id", nullable = false)
    private Tournament tournament;

    @Column(name = "prize_rank", nullable = false)
    private Integer rank;

    @Column(nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal amount = BigDecimal.ZERO;

    @Column(length = 255)
    private String itemName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private PrizeRecipientPolicy recipientPolicy = PrizeRecipientPolicy.OWNER;

    @Column(length = 1000)
    private String note;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
        if (amount == null) {
            amount = BigDecimal.ZERO;
        }
        if (recipientPolicy == null) {
            recipientPolicy = PrizeRecipientPolicy.OWNER;
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

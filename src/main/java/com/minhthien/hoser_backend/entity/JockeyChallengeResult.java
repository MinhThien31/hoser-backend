package com.minhthien.hoser_backend.entity;

import com.minhthien.hoser_backend.enums.RacePayoutStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "jockey_challenge_results",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_jockey_challenge_result", columnNames = {"tournament_id", "jockey_id"})
        },
        indexes = {
                @Index(name = "idx_jockey_challenge_results_tournament", columnList = "tournament_id")
        }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JockeyChallengeResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tournament_id", nullable = false)
    private Tournament tournament;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "jockey_id", nullable = false)
    private User jockey;

    @Column(nullable = false)
    private Integer totalPoints;

    @Column(nullable = false)
    private Integer firstPlaces;

    @Column(nullable = false)
    private Integer secondPlaces;

    @Column(nullable = false)
    private Integer thirdPlaces;

    private Integer challengeRank;

    @Column(nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal prizeAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private RacePayoutStatus payoutStatus = RacePayoutStatus.NOT_ELIGIBLE;

    private Long finalizedBy;

    private LocalDateTime finalizedAt;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (prizeAmount == null) {
            prizeAmount = BigDecimal.ZERO;
        }
        if (payoutStatus == null) {
            payoutStatus = RacePayoutStatus.NOT_ELIGIBLE;
        }
    }
}

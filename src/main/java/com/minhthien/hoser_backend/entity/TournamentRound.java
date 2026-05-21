package com.minhthien.hoser_backend.entity;

import com.minhthien.hoser_backend.enums.AdvancementRuleType;
import com.minhthien.hoser_backend.enums.TournamentRoundStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "tournament_rounds",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_tournament_round_order", columnNames = {"tournament_id", "round_order"})
        },
        indexes = {
                @Index(name = "idx_tournament_rounds_tournament", columnList = "tournament_id")
        }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TournamentRound {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tournament_id", nullable = false)
    private Tournament tournament;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(name = "round_order", nullable = false)
    private Integer roundOrder;

    @Column(nullable = false)
    private Integer raceCount;

    @Column(nullable = false)
    private Integer minParticipantsPerRace;

    @Column(nullable = false)
    private Integer maxParticipantsPerRace;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private AdvancementRuleType advancementRuleType = AdvancementRuleType.RANK;

    @Column(nullable = false)
    private Integer advancementCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private TournamentRoundStatus status = TournamentRoundStatus.CONFIGURED;

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
        if (advancementRuleType == null) {
            advancementRuleType = AdvancementRuleType.RANK;
        }
        if (status == null) {
            status = TournamentRoundStatus.CONFIGURED;
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

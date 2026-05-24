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
        name = "race_prizes",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_race_prize_rank", columnNames = {"race_id", "prize_rank"})
        },
        indexes = {
                @Index(name = "idx_race_prizes_race", columnList = "race_id")
        }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RacePrize {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "race_id", nullable = false)
    private Race race;

    @Column(name = "prize_rank", nullable = false)
    private Integer rank;

    @Column(nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal amount = BigDecimal.ZERO;

    @Column(length = 255)
    private String itemName;

    @Column(length = 1000)
    private String note;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (amount == null) {
            amount = BigDecimal.ZERO;
        }
    }
}

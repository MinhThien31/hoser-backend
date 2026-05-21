package com.minhthien.hoser_backend.entity;

import com.minhthien.hoser_backend.enums.TournamentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "tournaments",
        indexes = {
                @Index(name = "idx_tournaments_status", columnList = "status"),
                @Index(name = "idx_tournaments_start_at", columnList = "start_at")
        }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Tournament {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 160)
    private String name;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false, length = 255)
    private String location;

    @Column(name = "registration_open_at", nullable = false)
    private LocalDateTime registrationOpenAt;

    @Column(name = "registration_close_at", nullable = false)
    private LocalDateTime registrationCloseAt;

    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;

    @Column(name = "end_at", nullable = false)
    private LocalDateTime endAt;

    @Column(name = "check_in_deadline_at")
    private LocalDateTime checkInDeadlineAt;

    @Column(nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal entryFee = BigDecimal.ZERO;

    @Column(nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal depositAmount = BigDecimal.ZERO;

    @Column(nullable = false)
    private Integer minTeams;

    @Column(nullable = false)
    private Integer maxTeams;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private TournamentStatus status = TournamentStatus.DRAFT;

    private LocalDateTime publishedAt;

    private LocalDateTime openedRegistrationAt;

    @OneToMany(mappedBy = "tournament", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("roundOrder ASC")
    @Builder.Default
    private List<TournamentRound> rounds = new ArrayList<>();

    @OneToMany(mappedBy = "tournament", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("rank ASC")
    @Builder.Default
    private List<TournamentPrize> prizes = new ArrayList<>();

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
            status = TournamentStatus.DRAFT;
        }
        if (entryFee == null) {
            entryFee = BigDecimal.ZERO;
        }
        if (depositAmount == null) {
            depositAmount = BigDecimal.ZERO;
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

    public void replaceRounds(List<TournamentRound> newRounds) {
        rounds.clear();
        if (newRounds != null) {
            newRounds.forEach(round -> {
                round.setTournament(this);
                rounds.add(round);
            });
        }
    }

    public void replacePrizes(List<TournamentPrize> newPrizes) {
        prizes.clear();
        if (newPrizes != null) {
            newPrizes.forEach(prize -> {
                prize.setTournament(this);
                prizes.add(prize);
            });
        }
    }
}

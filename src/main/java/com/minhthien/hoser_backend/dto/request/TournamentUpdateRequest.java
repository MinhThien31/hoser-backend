package com.minhthien.hoser_backend.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class TournamentUpdateRequest {
    @Size(max = 160, message = "Tournament name must be at most 160 characters")
    private String name;

    @Size(max = 2000, message = "Description must be at most 2000 characters")
    private String description;

    @Size(max = 255, message = "Location must be at most 255 characters")
    private String location;

    private LocalDateTime registrationOpenAt;

    private LocalDateTime registrationCloseAt;

    private LocalDateTime startAt;

    private LocalDateTime endAt;

    private LocalDateTime checkInDeadlineAt;

    @PositiveOrZero(message = "Entry fee must not be negative")
    private BigDecimal entryFee;

    @PositiveOrZero(message = "Deposit amount must not be negative")
    private BigDecimal depositAmount;

    @Positive(message = "Minimum teams must be greater than zero")
    private Integer minTeams;

    @Positive(message = "Maximum teams must be greater than zero")
    private Integer maxTeams;

    @Valid
    private List<TournamentRoundRequest> rounds;

    @Valid
    private List<TournamentPrizeRequest> prizes;
}

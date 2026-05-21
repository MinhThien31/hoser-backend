package com.minhthien.hoser_backend.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class TournamentRequest {
    @NotBlank(message = "Tournament name is required")
    @Size(max = 160, message = "Tournament name must be at most 160 characters")
    private String name;

    @Size(max = 2000, message = "Description must be at most 2000 characters")
    private String description;

    @NotBlank(message = "Location is required")
    @Size(max = 255, message = "Location must be at most 255 characters")
    private String location;

    @NotNull(message = "Registration open time is required")
    private LocalDateTime registrationOpenAt;

    @NotNull(message = "Registration close time is required")
    private LocalDateTime registrationCloseAt;

    @NotNull(message = "Tournament start time is required")
    private LocalDateTime startAt;

    @NotNull(message = "Tournament end time is required")
    private LocalDateTime endAt;

    private LocalDateTime checkInDeadlineAt;

    @NotNull(message = "Entry fee is required")
    @PositiveOrZero(message = "Entry fee must not be negative")
    private BigDecimal entryFee = BigDecimal.ZERO;

    @NotNull(message = "Deposit amount is required")
    @PositiveOrZero(message = "Deposit amount must not be negative")
    private BigDecimal depositAmount = BigDecimal.ZERO;

    @NotNull(message = "Minimum teams is required")
    @Positive(message = "Minimum teams must be greater than zero")
    private Integer minTeams;

    @NotNull(message = "Maximum teams is required")
    @Positive(message = "Maximum teams must be greater than zero")
    private Integer maxTeams;

    @Valid
    private List<TournamentRoundRequest> rounds = new ArrayList<>();

    @Valid
    private List<TournamentPrizeRequest> prizes = new ArrayList<>();
}

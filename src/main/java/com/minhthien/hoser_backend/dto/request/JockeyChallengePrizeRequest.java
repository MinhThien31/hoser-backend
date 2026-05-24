package com.minhthien.hoser_backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class JockeyChallengePrizeRequest {
    @NotNull(message = "Challenge prize rank is required")
    @Positive(message = "Challenge prize rank must be greater than zero")
    @Schema(description = "Challenge leaderboard rank. Starts at 1.", example = "1")
    private Integer rank;

    @NotNull(message = "Challenge prize amount is required")
    @PositiveOrZero(message = "Challenge prize amount must not be negative")
    @Schema(description = "Prize amount paid to the jockey wallet for this challenge rank", example = "1000000")
    private BigDecimal amount = BigDecimal.ZERO;

    @Size(max = 1000, message = "Challenge prize note must be at most 1000 characters")
    @Schema(example = "Best jockey of the day")
    private String note;
}

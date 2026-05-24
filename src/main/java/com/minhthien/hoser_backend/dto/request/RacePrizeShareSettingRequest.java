package com.minhthien.hoser_backend.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class RacePrizeShareSettingRequest {
    @NotNull(message = "Rank is required")
    @Min(value = 1, message = "Rank must be greater than zero")
    private Integer rank;

    @NotNull(message = "Jockey percent is required")
    @DecimalMin(value = "0.00", message = "Jockey percent must be at least 0")
    @DecimalMax(value = "100.00", message = "Jockey percent must be at most 100")
    private BigDecimal jockeyPercent;
}

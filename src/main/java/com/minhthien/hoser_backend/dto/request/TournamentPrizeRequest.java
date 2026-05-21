package com.minhthien.hoser_backend.dto.request;

import com.minhthien.hoser_backend.enums.PrizeRecipientPolicy;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TournamentPrizeRequest {
    @NotNull(message = "Prize rank is required")
    @Positive(message = "Prize rank must be greater than zero")
    private Integer rank;

    @NotNull(message = "Prize amount is required")
    @PositiveOrZero(message = "Prize amount must not be negative")
    private BigDecimal amount = BigDecimal.ZERO;

    @Size(max = 255, message = "Prize item name must be at most 255 characters")
    private String itemName;

    private PrizeRecipientPolicy recipientPolicy = PrizeRecipientPolicy.OWNER;

    @Size(max = 1000, message = "Prize note must be at most 1000 characters")
    private String note;
}

package com.minhthien.hoser_backend.dto.request;

import com.minhthien.hoser_backend.enums.AdvancementRuleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TournamentRoundRequest {
    @NotBlank(message = "Round name is required")
    @Size(max = 120, message = "Round name must be at most 120 characters")
    private String name;

    @NotNull(message = "Round order is required")
    @Positive(message = "Round order must be greater than zero")
    private Integer roundOrder;

    @NotNull(message = "Race count is required")
    @Positive(message = "Race count must be greater than zero")
    private Integer raceCount;

    @NotNull(message = "Minimum participants per race is required")
    @Positive(message = "Minimum participants per race must be greater than zero")
    private Integer minParticipantsPerRace;

    @NotNull(message = "Maximum participants per race is required")
    @Positive(message = "Maximum participants per race must be greater than zero")
    private Integer maxParticipantsPerRace;

    private AdvancementRuleType advancementRuleType = AdvancementRuleType.RANK;

    @NotNull(message = "Advancement count is required")
    @Positive(message = "Advancement count must be greater than zero")
    private Integer advancementCount;

    @Size(max = 1000, message = "Round note must be at most 1000 characters")
    private String note;
}

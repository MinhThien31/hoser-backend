package com.minhthien.hoser_backend.dto.response;

import com.minhthien.hoser_backend.enums.AdvancementRuleType;
import com.minhthien.hoser_backend.enums.TournamentRoundStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class TournamentRoundResponse {
    private Long id;
    private String name;
    private Integer roundOrder;
    private Integer raceCount;
    private Integer minParticipantsPerRace;
    private Integer maxParticipantsPerRace;
    private AdvancementRuleType advancementRuleType;
    private Integer advancementCount;
    private TournamentRoundStatus status;
    private String note;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

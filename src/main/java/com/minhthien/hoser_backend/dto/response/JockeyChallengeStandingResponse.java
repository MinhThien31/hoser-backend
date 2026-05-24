package com.minhthien.hoser_backend.dto.response;

import com.minhthien.hoser_backend.enums.RacePayoutStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class JockeyChallengeStandingResponse {
    private Long jockeyId;
    private String jockeyUsername;
    private Integer totalPoints;
    private Integer firstPlaces;
    private Integer secondPlaces;
    private Integer thirdPlaces;
    private Integer challengeRank;
    private BigDecimal prizeAmount;
    private RacePayoutStatus payoutStatus;
    private LocalDateTime finalizedAt;
}

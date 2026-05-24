package com.minhthien.hoser_backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class AdminPayoutDebtResponse {
    private String debtType;
    private Long referenceId;
    private Long tournamentId;
    private String tournamentName;
    private Long raceId;
    private String raceName;
    private Long recipientUserId;
    private String recipientUsername;
    private String recipientRole;
    private Long horseId;
    private String horseName;
    private Long jockeyId;
    private String jockeyUsername;
    private Integer rank;
    private BigDecimal amount;
    private BigDecimal ownerPrizeAmount;
    private BigDecimal jockeyPrizeAmount;
    private BigDecimal jockeyPrizePercent;
    private LocalDateTime finalizedAt;
    private String note;
}

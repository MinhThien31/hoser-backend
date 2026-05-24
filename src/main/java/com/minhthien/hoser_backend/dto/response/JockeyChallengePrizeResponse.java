package com.minhthien.hoser_backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class JockeyChallengePrizeResponse {
    private Long id;
    private Integer rank;
    private BigDecimal amount;
    private String note;
    private LocalDateTime createdAt;
}

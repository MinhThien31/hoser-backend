package com.minhthien.hoser_backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class RacePrizeResponse {
    private Long id;
    private Integer rank;
    private BigDecimal amount;
    private String itemName;
    private String note;
    private LocalDateTime createdAt;
}

package com.minhthien.hoser_backend.dto.response;

import com.minhthien.hoser_backend.enums.PrizeRecipientPolicy;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class TournamentPrizeResponse {
    private Long id;
    private Integer rank;
    private BigDecimal amount;
    private String itemName;
    private PrizeRecipientPolicy recipientPolicy;
    private String note;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

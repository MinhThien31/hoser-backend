package com.minhthien.hoser_backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class RacePrizeShareSettingResponse {
    private Integer rank;
    private BigDecimal ownerPercent;
    private BigDecimal jockeyPercent;
}

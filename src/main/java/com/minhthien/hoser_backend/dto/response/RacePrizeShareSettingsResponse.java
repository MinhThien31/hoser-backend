package com.minhthien.hoser_backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RacePrizeShareSettingsResponse {
    private List<RacePrizeShareSettingResponse> shares;
}

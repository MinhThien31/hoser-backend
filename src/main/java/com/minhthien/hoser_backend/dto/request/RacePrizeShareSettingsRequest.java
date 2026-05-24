package com.minhthien.hoser_backend.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class RacePrizeShareSettingsRequest {
    @Valid
    @NotNull(message = "Shares are required")
    private List<RacePrizeShareSettingRequest> shares = new ArrayList<>();
}

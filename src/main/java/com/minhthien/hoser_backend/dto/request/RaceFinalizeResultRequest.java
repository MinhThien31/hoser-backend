package com.minhthien.hoser_backend.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class RaceFinalizeResultRequest {
    @Valid
    @NotEmpty(message = "Race results are required")
    private List<RaceResultEntryRequest> results = new ArrayList<>();
}

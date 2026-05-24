package com.minhthien.hoser_backend.dto.request;

import com.minhthien.hoser_backend.enums.RaceParticipantStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RaceResultEntryRequest {
    @NotNull(message = "Participant id is required")
    private Long participantId;

    @Positive(message = "Rank must be greater than zero")
    private Integer rank;

    @PositiveOrZero(message = "Finish time must not be negative")
    private Long finishTimeMillis;

    @NotNull(message = "Result status is required")
    private RaceParticipantStatus status;

    @Size(max = 1000, message = "Result note must be at most 1000 characters")
    private String note;
}

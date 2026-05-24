package com.minhthien.hoser_backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RaceRegistrationRequest {
    @NotNull(message = "Horse id is required")
    @Schema(description = "Approved horse owned by the current owner", example = "1")
    private Long horseId;

    @NotNull(message = "Jockey invitation id is required")
    @Schema(description = "Accepted owner-jockey invitation for this horse", example = "10")
    private Long jockeyInvitationId;

    @Size(max = 1000, message = "Registration note must be at most 1000 characters")
    private String note;
}

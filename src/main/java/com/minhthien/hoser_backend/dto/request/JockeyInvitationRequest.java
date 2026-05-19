package com.minhthien.hoser_backend.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class JockeyInvitationRequest {
    @NotNull(message = "Horse id is required")
    private Long horseId;

    @NotNull(message = "Jockey id is required")
    private Long jockeyId;

    @Size(max = 1000, message = "Message must be at most 1000 characters")
    private String message;
}

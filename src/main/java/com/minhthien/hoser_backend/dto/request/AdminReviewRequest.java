package com.minhthien.hoser_backend.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdminReviewRequest {
    @Size(max = 1000, message = "Reason must be at most 1000 characters")
    private String reason;
}

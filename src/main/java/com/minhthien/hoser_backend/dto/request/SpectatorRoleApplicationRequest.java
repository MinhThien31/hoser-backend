package com.minhthien.hoser_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SpectatorRoleApplicationRequest {
    @NotBlank(message = "Display name is required")
    @Size(max = 100, message = "Display name must be at most 100 characters")
    private String displayName;

    @Size(max = 30, message = "Phone must be at most 30 characters")
    private String phone;

    @Size(max = 255, message = "Location must be at most 255 characters")
    private String location;

    @Size(max = 120, message = "Favorite horse breed must be at most 120 characters")
    private String favoriteHorseBreed;

    @Size(max = 1000, message = "Bio must be at most 1000 characters")
    private String bio;
}

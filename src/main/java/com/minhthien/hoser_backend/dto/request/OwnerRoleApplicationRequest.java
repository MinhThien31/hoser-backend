package com.minhthien.hoser_backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class OwnerRoleApplicationRequest {
    @NotBlank(message = "Stable name is required")
    @Size(max = 160, message = "Stable name must be at most 160 characters")
    private String stableName;

    @Min(value = 0, message = "Experience years must be positive")
    private Integer experienceYears;

    @NotBlank(message = "Address is required")
    @Size(max = 255, message = "Address must be at most 255 characters")
    private String address;

    @Size(max = 1000, message = "Bio must be at most 1000 characters")
    private String bio;

    @Schema(type = "string", format = "binary", description = "Owner verification document")
    private MultipartFile verificationDocument;
}

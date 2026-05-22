package com.minhthien.hoser_backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class RefereeRoleApplicationRequest {
    @NotBlank(message = "License number is required")
    @Size(max = 100, message = "License number must be at most 100 characters")
    private String licenseNumber;

    @Min(value = 0, message = "Experience years must be positive")
    private Integer experienceYears;

    @NotBlank(message = "Specialty is required")
    @Size(max = 160, message = "Specialty must be at most 160 characters")
    private String specialty;

    @Size(max = 1000, message = "Bio must be at most 1000 characters")
    private String bio;

    @Schema(type = "string", format = "binary", description = "Referee certification document")
    private MultipartFile certificationDocument;
}

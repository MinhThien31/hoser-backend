package com.minhthien.hoser_backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

@Data
public class JockeyProfileRequest {
    @NotBlank(message = "License number is required")
    @Size(max = 100, message = "License number must be at most 100 characters")
    private String licenseNumber;

    @Min(value = 0, message = "Experience years must be positive")
    private Integer experienceYears;

    private BigDecimal heightCm;

    private BigDecimal weightKg;

    @NotNull(message = "Hire price is required")
    @DecimalMin(value = "0.01", message = "Hire price must be greater than zero")
    private BigDecimal hirePrice;

    @Size(max = 1000, message = "Bio must be at most 1000 characters")
    private String bio;

    @Size(max = 2000, message = "Awards must be at most 2000 characters")
    private String awards;

    @Size(max = 2000, message = "Achievements must be at most 2000 characters")
    private String achievements;

    @Size(max = 1000, message = "Specialties must be at most 1000 characters")
    private String specialties;

    @Schema(type = "string", format = "binary", description = "Jockey avatar image file")
    private MultipartFile avatar;

    @Schema(type = "string", format = "binary", description = "Jockey license document file")
    private MultipartFile licenseDocument;
}

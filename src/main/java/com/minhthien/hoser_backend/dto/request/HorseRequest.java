package com.minhthien.hoser_backend.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class HorseRequest {
    @NotBlank(message = "Horse name is required")
    @Size(max = 120, message = "Horse name must be at most 120 characters")
    private String name;

    @Size(max = 120, message = "Breed must be at most 120 characters")
    private String breed;

    @Min(value = 0, message = "Age must be positive")
    private Integer age;

    @Size(max = 40, message = "Gender must be at most 40 characters")
    private String gender;

    @Size(max = 80, message = "Color must be at most 80 characters")
    private String color;

    private BigDecimal heightCm;

    private BigDecimal weightKg;

    @Size(max = 1000, message = "Image URL must be at most 1000 characters")
    private String imageUrl;

    @Size(max = 1000, message = "Document URL must be at most 1000 characters")
    private String documentUrl;
}

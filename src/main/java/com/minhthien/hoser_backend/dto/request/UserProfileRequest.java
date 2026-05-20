package com.minhthien.hoser_backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class UserProfileRequest {
    @Size(max = 100, message = "Full name must be at most 100 characters")
    private String fullName;

    @Size(max = 30, message = "Phone must be at most 30 characters")
    private String phone;

    @Size(max = 255, message = "Location must be at most 255 characters")
    private String location;

    @Schema(type = "string", format = "binary", description = "User avatar image file")
    private MultipartFile avatar;
}

package com.example.hrms.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateAvatarRequest {
    @NotBlank(message = "Avatar base64 is required")
    private String avatarBase64;
}


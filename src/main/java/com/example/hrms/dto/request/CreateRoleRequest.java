package com.example.hrms.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateRoleRequest {
    @NotBlank(message = "Role key is required")
    private String roleKey;

    @NotBlank(message = "Role name is required")
    private String roleName;
}


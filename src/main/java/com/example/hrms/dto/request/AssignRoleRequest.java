package com.example.hrms.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssignRoleRequest {
    @NotNull(message = "Role ID is required")
    private Integer roleId;
}


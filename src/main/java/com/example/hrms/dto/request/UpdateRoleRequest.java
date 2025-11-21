package com.example.hrms.dto.request;

import lombok.Data;

@Data
public class UpdateRoleRequest {
    private String roleKey;
    private String roleName;
}


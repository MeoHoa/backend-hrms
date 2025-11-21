package com.example.hrms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleResponse {
    private Integer roleId;
    private String roleKey;
    private String roleName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}


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
public class UserResponse {
    private Integer userId;
    private String username;
    private String email;
    private Integer roleId;
    private String roleKey;
    private String roleName;
    private Integer employeeId;
    private Boolean active;
    private String avatarBase64;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}


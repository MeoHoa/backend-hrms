package com.example.hrms.dto.request;

import lombok.Data;

@Data
public class UpdateUserRequest {
    private String username;
    private String email;
    private Integer roleId;
    private Integer employeeId;
    private Boolean active;
    private String avatarBase64;
}


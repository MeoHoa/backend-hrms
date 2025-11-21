package com.example.hrms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserInfoResponse {
    private Integer id;
    private String username;
    private String email;
    private String role;
    private Integer employeeId;
    private Integer status;
}


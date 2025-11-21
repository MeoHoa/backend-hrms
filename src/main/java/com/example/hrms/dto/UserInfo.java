package com.example.hrms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserInfo {
    private Integer id;
    private String username;
    private String email;
    private String phoneNumber;
    private String address;
    private String role;
    private Integer status;
    private String avatarBase64;
}

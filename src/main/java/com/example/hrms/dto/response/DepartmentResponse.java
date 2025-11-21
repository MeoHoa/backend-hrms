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
public class DepartmentResponse {
    private Integer departmentId;
    private String departmentName;
    private String description;
    private Integer adminId;
    private String adminName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}


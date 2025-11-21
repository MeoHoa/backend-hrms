package com.example.hrms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Simplified DTO for leave type dropdown (only id and name)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveTypeSimpleResponse {
    private Integer id;
    private String name;
}


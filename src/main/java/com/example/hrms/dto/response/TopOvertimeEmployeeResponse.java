package com.example.hrms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopOvertimeEmployeeResponse {
    private Integer employeeId;
    private String employeeName;
    private String employeeEmail;
    private String departmentName;
    private BigDecimal overtimeHours;
}


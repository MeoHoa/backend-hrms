package com.example.hrms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimekeepingSummaryResponse {
    private Integer employeeId;
    private String employeeName;
    private String employeeEmail;
    private Integer totalWorkDays;
    private BigDecimal totalWorkHours;
    private BigDecimal totalOvertimeHours;
    private Integer lateArrivals;
    private Integer earlyDepartures;
    private BigDecimal attendanceRate;
}


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
public class DashboardStatsResponse {
    private Integer totalEmployees;
    private Integer activeEmployees;
    private Integer totalDepartments;
    private Integer pendingLeaveRequests;
    private Integer pendingTimekeepingRecords;
    private BigDecimal overallAttendanceRate;
    private Integer todayCheckIns;
    private Integer todayAbsences;
}


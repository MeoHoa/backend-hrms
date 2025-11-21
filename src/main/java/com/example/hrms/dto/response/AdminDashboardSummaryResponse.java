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
public class AdminDashboardSummaryResponse {
    private Integer totalEmployees;
    private Integer activeEmployees;
    private Integer inactiveEmployees;
    private Integer totalDepartments;
    private Integer pendingTimekeepingRecords;
    private Integer pendingLeaveRequests;
    private BigDecimal overtimeHoursThisMonth;
    private Integer todayCheckIns;
    private Integer todayAbsences;
}


package com.example.hrms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleCoverageResponse {
    private LocalDate rangeStart;
    private LocalDate rangeEnd;
    private Integer totalSchedules;
    private Integer uniqueDepartments;
    private Integer uniqueEmployees;
    private BigDecimal averageCoverageRatio;
    private List<DayCoverage> dailyCoverage;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DayCoverage {
        private LocalDate date;
        private Integer totalEvents;
        private Integer departmentsInvolved;
        private Integer employeesScheduled;
        private Integer availableEmployees;
        private Integer shortage;
        private BigDecimal coverageRatio;
    }
}


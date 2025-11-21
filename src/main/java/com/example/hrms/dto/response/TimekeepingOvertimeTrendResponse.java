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
public class TimekeepingOvertimeTrendResponse {
    private LocalDate rangeStart;
    private LocalDate rangeEnd;
    private Integer departmentId;
    private String departmentName;
    private BigDecimal totalOvertimeHours;
    private List<OvertimePoint> points;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OvertimePoint {
        private LocalDate date;
        private BigDecimal overtimeHours;
    }
}


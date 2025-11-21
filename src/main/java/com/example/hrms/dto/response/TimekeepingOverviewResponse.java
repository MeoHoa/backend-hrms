package com.example.hrms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimekeepingOverviewResponse {
    private LocalDate rangeStart;
    private LocalDate rangeEnd;
    private Long totalPending;
    private Long totalConfirmed;
    private Long totalError;
    private List<DailyStatusBreakdown> dailyBreakdown;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyStatusBreakdown {
        private LocalDate date;
        private Long pending;
        private Long confirmed;
        private Long error;
    }
}


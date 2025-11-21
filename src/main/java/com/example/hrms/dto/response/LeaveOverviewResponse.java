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
public class LeaveOverviewResponse {
    private LocalDate rangeStart;
    private LocalDate rangeEnd;
    private Integer totalRequests;
    private Integer pendingRequests;
    private Integer approvedRequests;
    private Integer rejectedRequests;
    private Integer uniqueEmployees;
    private List<LeaveDailySummary> dailyBreakdown;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LeaveDailySummary {
        private LocalDate date;
        private Integer pending;
        private Integer approved;
        private Integer rejected;
    }
}


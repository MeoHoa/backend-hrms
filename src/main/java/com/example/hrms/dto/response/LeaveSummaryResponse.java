package com.example.hrms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveSummaryResponse {
    private Integer totalLeaveDays;
    private Integer approvedLeaveDays;
    private Integer pendingLeaveDays;
    private Integer rejectedLeaveDays;
    private Integer annualLeaveEntitlement;
    private Integer usedAnnualLeaveDays;
    private Integer remainingAnnualLeaveDays;
    private Map<String, Integer> leaveDaysByType; // leave type name -> days
    private List<LeaveTypeSummary> leaveTypeDetails;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LeaveTypeSummary {
        private String leaveTypeName;
        private Integer totalDays;
        private Integer approvedDays;
        private Integer pendingDays;
    }
}


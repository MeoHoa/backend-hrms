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
public class EmployeeAlertResponse {
    private List<AlertItem> alerts;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AlertItem {
        private Integer employeeId;
        private String employeeName;
        private String employeeEmail;
        private String departmentName;
        private String alertType;
        private String message;
        private LocalDate fromDate;
        private LocalDate toDate;
    }
}


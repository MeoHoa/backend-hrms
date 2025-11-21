package com.example.hrms.dto.response;

import com.example.hrms.entity.OnLeave;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveRequestResponse {
    private Integer requestId;
    private Integer employeeId;
    private String employeeFullName;
    private String employeeEmail;
    private Integer userId; // Optional: included if employee has associated user
    private Integer leaveTypeId;
    private String leaveType;
    private String reason;
    private LocalDate fromDate;
    private LocalDate toDate;
    private OnLeave.Status status;
    private Integer adminId;
    private String adminFullName;
    private String rejectionReason;
    private LocalDateTime processedDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}


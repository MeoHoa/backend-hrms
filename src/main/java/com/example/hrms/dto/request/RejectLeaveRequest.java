package com.example.hrms.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RejectLeaveRequest {
    @NotBlank(message = "Rejection reason is required")
    private String rejectionReason;
}


package com.example.hrms.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO for employee to update their own timekeeping reason
 * Employee can only update the reason field, not check-in/check-out times
 */
@Data
public class UpdateMyTimekeepingReasonRequest {
    @NotBlank(message = "Reason is required")
    private String reason; // Lý do chấm công bất thường
}


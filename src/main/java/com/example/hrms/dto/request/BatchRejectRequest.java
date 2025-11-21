package com.example.hrms.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * DTO for batch rejecting timekeeping records
 */
@Data
public class BatchRejectRequest {
    @NotEmpty(message = "Record IDs list cannot be empty")
    private List<Integer> recordIds;
    
    private String reason; // Reason for rejection
}


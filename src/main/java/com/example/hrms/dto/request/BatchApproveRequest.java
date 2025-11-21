package com.example.hrms.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * DTO for batch approving timekeeping records
 */
@Data
public class BatchApproveRequest {
    @NotEmpty(message = "Record IDs list cannot be empty")
    private List<Integer> recordIds;
    
    private String adminNote; // Optional note for all records
}


package com.example.hrms.dto.request;

import lombok.Data;

/**
 * DTO for approving timekeeping record
 * Admin can optionally add a note when approving
 */
@Data
public class ApproveTimekeepingRequest {
    private String adminNote; // Optional note when approving
}


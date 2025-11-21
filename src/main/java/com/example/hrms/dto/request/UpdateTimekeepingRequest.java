package com.example.hrms.dto.request;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * DTO for updating timekeeping record by admin
 * Used to fix errors, especially when employee forgets to check-out
 */
@Data
public class UpdateTimekeepingRequest {
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime checkIn;
    
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime checkOut;
    
    private String reason; // Lý do bất thường (UI controls)
    
    private String adminNote; // Ghi chú của admin khi sửa
}


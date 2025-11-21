package com.example.hrms.dto.response;

import com.example.hrms.entity.Timekeeping;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimekeepingResponse {
    private Integer id;
    private Integer employeeId;
    private String employeeFullName;
    private String employeeEmail;
    private LocalDateTime checkIn;
    private LocalDateTime checkOut;
    private LocalDate workDate;
    private BigDecimal requiredWorkHours; // Số giờ làm yêu cầu
    private BigDecimal workHours; // Số giờ làm thực tế
    private BigDecimal overtimeHours;
    
    // Expected times (for UI display)
    private LocalTime expectedCheckInTime;
    private LocalTime expectedCheckOutTime;
    
    // Reason for abnormal timekeeping (UI controls)
    private String reason;
    
    // Admin notes
    private String adminNote;
    
    // Holiday information
    private String holidayName; // Name of holiday if workDate is a holiday (e.g., "Tết Dương lịch")
    private Boolean isHoliday; // true if workDate is a holiday
    
    private Timekeeping.Status status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

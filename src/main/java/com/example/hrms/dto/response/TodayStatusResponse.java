package com.example.hrms.dto.response;

import com.example.hrms.entity.Timekeeping;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO to show today's timekeeping status for employee
 * Helps frontend decide which button to display: Check-in, Check-out, or Done
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TodayStatusResponse {
    private boolean hasRecord;
    private boolean canCheckIn;
    private boolean canCheckOut;
    private boolean isCompleted;
    private LocalDate workDate;
    private LocalDateTime checkIn;
    private LocalDateTime checkOut;
    private Timekeeping.Status status;
    private String actionNeeded; // "CHECK_IN", "CHECK_OUT", "NONE"
    
    // Holiday information
    private Boolean isHoliday; // true if workDate is a holiday
    private String holidayName; // Name of holiday if workDate is a holiday
}


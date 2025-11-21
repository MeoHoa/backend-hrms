package com.example.hrms.dto.response;

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
public class HolidayResponse {
    private Integer holidayId;
    private String holidayName;
    private LocalDate holidayDate;
    private LocalDateTime createdAt;
}


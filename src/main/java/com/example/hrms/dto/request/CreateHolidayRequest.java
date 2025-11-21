package com.example.hrms.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateHolidayRequest {
    @NotBlank(message = "Holiday name is required")
    private String holidayName;

    @NotNull(message = "Holiday date is required")
    private LocalDate holidayDate;
}


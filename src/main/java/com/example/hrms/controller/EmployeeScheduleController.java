package com.example.hrms.controller;

import com.example.hrms.dto.base.ApiResponse;
import com.example.hrms.service.ScheduleService;
import com.example.hrms.util.ResponseHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import com.example.hrms.dto.response.*;
@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
public class EmployeeScheduleController {
    private final ScheduleService scheduleService;

    @GetMapping("/my-schedule")
    public ResponseEntity<ApiResponse<List<ScheduleResponse>>> getMySchedule(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {
        List<ScheduleResponse> schedules = scheduleService.getMySchedule(month, year);
        return ResponseHelper.success("Schedule retrieved successfully", schedules);
    }
    
    @GetMapping("/my-shifts")
    public ResponseEntity<ApiResponse<List<ScheduleResponse>>> getMyShifts(
            @RequestParam(required = false) String range) {
        List<ScheduleResponse> schedules = scheduleService.getMyShifts(range);
        return ResponseHelper.success("Upcoming shifts retrieved successfully", schedules);
    }
}


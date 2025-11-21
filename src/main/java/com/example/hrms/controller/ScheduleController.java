package com.example.hrms.controller;

import com.example.hrms.dto.base.ApiResponse;
import com.example.hrms.service.ScheduleService;
import com.example.hrms.util.ResponseHelper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import com.example.hrms.dto.request.*;
import com.example.hrms.dto.response.*;
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/schedules")
public class ScheduleController {
    private final ScheduleService scheduleService;

    @PostMapping
    public ResponseEntity<ApiResponse<ScheduleResponse>> createSchedule(@Valid @RequestBody CreateScheduleRequest request) {
        ScheduleResponse response = scheduleService.createSchedule(request);
        return ResponseHelper.created("Schedule created successfully", response);
    }

    @GetMapping("/all-employees")
    public ResponseEntity<ApiResponse<List<ScheduleResponse>>> getAllEmployeesSchedules(
            @RequestParam(required = false) Integer departmentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<ScheduleResponse> schedules = scheduleService.getAllEmployeesSchedules(departmentId, date);
        return ResponseHelper.success("Schedules retrieved successfully", schedules);
    }

    @PutMapping("/{workId}")
    public ResponseEntity<ApiResponse<ScheduleResponse>> updateSchedule(
            @PathVariable Integer workId,
            @Valid @RequestBody UpdateScheduleRequest request) {
        ScheduleResponse response = scheduleService.updateSchedule(workId, request);
        return ResponseHelper.success("Schedule updated successfully", response);
    }
}

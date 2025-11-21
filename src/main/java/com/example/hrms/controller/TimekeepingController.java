package com.example.hrms.controller;

import com.example.hrms.dto.base.ApiResponse;
import com.example.hrms.service.TimekeepingService;
import com.example.hrms.util.ResponseHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import com.example.hrms.dto.request.*;
import com.example.hrms.dto.response.*;
@RestController
@RequestMapping("/api/timekeeping")
@RequiredArgsConstructor
public class TimekeepingController {
    private final TimekeepingService timekeepingService;

    @GetMapping("/today-status")
    public ResponseEntity<ApiResponse<TodayStatusResponse>> getTodayStatus() {
        TodayStatusResponse response = timekeepingService.getTodayStatus();
        return ResponseHelper.success("Today's timekeeping status retrieved successfully", response);
    }

    @PostMapping("/check-in")
    public ResponseEntity<ApiResponse<TimekeepingResponse>> checkIn(
            @RequestBody(required = false) CheckInRequest request) {
        TimekeepingResponse response = timekeepingService.checkIn(request);
        return ResponseHelper.created("Check-in successful", response);
    }

    @PostMapping("/check-out")
    public ResponseEntity<ApiResponse<TimekeepingResponse>> checkOut(
            @RequestBody(required = false) CheckOutRequest request) {
        TimekeepingResponse response = timekeepingService.checkOut(request);
        return ResponseHelper.success("Check-out successful", response);
    }

    @GetMapping("/my-history")
    public ResponseEntity<ApiResponse<List<TimekeepingResponse>>> getMyHistory(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer limit) {
        List<TimekeepingResponse> history = timekeepingService.getMyHistory(startDate, endDate, month, year, limit);
        return ResponseHelper.success("Timekeeping history retrieved successfully", history);
    }

    @PutMapping("/{recordId}")
    public ResponseEntity<ApiResponse<TimekeepingResponse>> updateMyTimekeepingReason(
            @PathVariable Integer recordId,
            @jakarta.validation.Valid @RequestBody UpdateMyTimekeepingReasonRequest request) {
        TimekeepingResponse response = timekeepingService.updateMyTimekeepingReason(recordId, request);
        return ResponseHelper.success("Timekeeping reason updated successfully", response);
    }
}

package com.example.hrms.controller;

import com.example.hrms.dto.base.ApiResponse;
import com.example.hrms.service.ReportService;
import com.example.hrms.util.ResponseHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import com.example.hrms.dto.request.*;
import com.example.hrms.dto.response.*;
@RestController
@RequestMapping("/api/admin/reports")
@RequiredArgsConstructor
public class ReportsController {
    private final ReportService reportService;

    @GetMapping("/timekeeping-summary")
    public ResponseEntity<ApiResponse<List<TimekeepingSummaryResponse>>> getTimekeepingSummary(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer employeeId) {
        List<TimekeepingSummaryResponse> summary = reportService.getTimekeepingSummary(month, year, employeeId);
        return ResponseHelper.success("Timekeeping summary retrieved successfully", summary);
    }

    @GetMapping("/leave-summary")
    public ResponseEntity<ApiResponse<LeaveSummaryResponse>> getLeaveSummary(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {
        LeaveSummaryResponse summary = reportService.getLeaveSummary(month, year);
        return ResponseHelper.success("Leave summary retrieved successfully", summary);
    }

    @GetMapping("/dashboard-stats")
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getDashboardStats() {
        DashboardStatsResponse stats = reportService.getDashboardStats();
        return ResponseHelper.success("Dashboard statistics retrieved successfully", stats);
    }
}

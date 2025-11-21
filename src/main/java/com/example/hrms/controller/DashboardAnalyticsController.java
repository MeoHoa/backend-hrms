package com.example.hrms.controller;

import com.example.hrms.dto.base.ApiResponse;
import com.example.hrms.dto.response.*;
import com.example.hrms.service.DashboardService;
import com.example.hrms.service.LeaveService;
import com.example.hrms.util.ResponseHelper;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class DashboardAnalyticsController {
    private final DashboardService dashboardService;
    private final LeaveService leaveService;

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<AdminDashboardSummaryResponse>> getAdminSummary() {
        AdminDashboardSummaryResponse response = dashboardService.getAdminSummary();
        return ResponseHelper.success("Dashboard summary fetched", response);
    }

    @GetMapping("/timekeeping-overview")
    public ResponseEntity<ApiResponse<TimekeepingOverviewResponse>> getTimekeepingOverview(
            @RequestParam(required = false) String range,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        TimekeepingOverviewResponse response = dashboardService.getTimekeepingOverview(range, startDate, endDate);
        return ResponseHelper.success("Timekeeping overview fetched", response);
    }

    @GetMapping("/overtime-trend")
    public ResponseEntity<ApiResponse<TimekeepingOvertimeTrendResponse>> getOvertimeTrend(
            @RequestParam(required = false) String range,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Integer departmentId) {
        TimekeepingOvertimeTrendResponse response = dashboardService.getOvertimeTrend(range, startDate, endDate, departmentId);
        return ResponseHelper.success("Timekeeping overtime trend fetched", response);
    }

    @GetMapping("/pending-timekeeping")
    public ResponseEntity<ApiResponse<List<TimekeepingResponse>>> getPendingTimekeeping(
            @RequestParam(defaultValue = "5") @Min(1) Integer limit) {
        int resolvedLimit = Math.min(limit, 50);
        List<TimekeepingResponse> response = dashboardService.getPendingTimekeeping(resolvedLimit);
        return ResponseHelper.success("Pending timekeeping approvals fetched", response);
    }

    @GetMapping("/leave-overview")
    public ResponseEntity<ApiResponse<LeaveOverviewResponse>> getLeaveOverview(
            @RequestParam(required = false) String range,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        LeaveOverviewResponse response = dashboardService.getLeaveOverview(range, startDate, endDate);
        return ResponseHelper.success("Leave overview fetched", response);
    }

    @GetMapping("/pending-leave")
    public ResponseEntity<ApiResponse<List<LeaveRequestResponse>>> getPendingLeaves(
            @RequestParam(defaultValue = "5") @Min(1) Integer limit) {
        int resolvedLimit = Math.min(limit, 50);
        List<LeaveRequestResponse> response = leaveService
                .getPendingLeaveRequests(PageRequest.of(0, resolvedLimit,
                        Sort.by(Sort.Direction.DESC, "createdAt")))
                .getContent();
        return ResponseHelper.success("Pending leave requests fetched", response);
    }

    @GetMapping("/upcoming-holidays")
    public ResponseEntity<ApiResponse<List<HolidayResponse>>> getUpcomingHolidays(
            @RequestParam(defaultValue = "5") @Min(1) Integer limit) {
        int resolvedLimit = Math.min(limit, 20);
        List<HolidayResponse> response = dashboardService.getUpcomingHolidays(resolvedLimit);
        return ResponseHelper.success("Upcoming holidays fetched", response);
    }

    @GetMapping("/schedule-coverage")
    public ResponseEntity<ApiResponse<ScheduleCoverageResponse>> getScheduleCoverage(
            @RequestParam(required = false) String range,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        ScheduleCoverageResponse response = dashboardService.getScheduleCoverage(range, startDate, endDate);
        return ResponseHelper.success("Schedule coverage fetched", response);
    }

    @GetMapping("/employee-alerts")
    public ResponseEntity<ApiResponse<EmployeeAlertResponse>> getEmployeeAlerts(
            @RequestParam(defaultValue = "10") @Min(1) Integer limit) {
        int resolvedLimit = Math.min(limit, 100);
        EmployeeAlertResponse response = dashboardService.getEmployeeAlerts(resolvedLimit);
        return ResponseHelper.success("Employee alerts fetched", response);
    }
    
    @GetMapping("/top-overtime-employees")
    public ResponseEntity<ApiResponse<List<TopOvertimeEmployeeResponse>>> getTopOvertimeEmployees(
            @RequestParam(defaultValue = "5") @Min(1) Integer limit) {
        int resolvedLimit = Math.min(limit, 50);
        List<TopOvertimeEmployeeResponse> response = dashboardService.getTopOvertimeEmployees(resolvedLimit);
        return ResponseHelper.success("Top overtime employees fetched", response);
    }
}


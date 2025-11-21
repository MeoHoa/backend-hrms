package com.example.hrms.controller;

import com.example.hrms.dto.base.ApiResponse;
import com.example.hrms.service.LeaveService;
import com.example.hrms.util.ResponseHelper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import com.example.hrms.dto.request.*;
import com.example.hrms.dto.response.*;
@RestController
@RequestMapping("/api/leave-requests")
@RequiredArgsConstructor
public class LeaveController {
    private final LeaveService leaveService;

    @PostMapping
    public ResponseEntity<ApiResponse<LeaveRequestResponse>> createLeaveRequest(@Valid @RequestBody CreateLeaveRequest request) {
        LeaveRequestResponse response = leaveService.createLeaveRequest(request);
        return ResponseHelper.created("Leave request created successfully", response);
    }

    @GetMapping("/my-requests")
    public ResponseEntity<ApiResponse<List<LeaveRequestResponse>>> getMyLeaveRequests(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer limit) {
        List<LeaveRequestResponse> requests = leaveService.getMyLeaveRequests(status, month, year, limit);
        return ResponseHelper.success("Leave requests retrieved successfully", requests);
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<LeaveSummaryResponse>> getLeaveSummary() {
        LeaveSummaryResponse summary = leaveService.getMyLeaveSummary();
        return ResponseHelper.success("Leave summary retrieved successfully", summary);
    }

    @PutMapping("/{requestId}")
    public ResponseEntity<ApiResponse<LeaveRequestResponse>> updateLeaveRequest(
            @PathVariable Integer requestId,
            @Valid @RequestBody UpdateLeaveRequest request) {
        LeaveRequestResponse response = leaveService.updateLeaveRequest(requestId, request);
        return ResponseHelper.success("Leave request updated successfully", response);
    }

    @DeleteMapping("/{requestId}")
    public ResponseEntity<ApiResponse<Void>> deleteLeaveRequest(@PathVariable Integer requestId) {
        leaveService.deleteLeaveRequest(requestId);
        return ResponseHelper.success("Leave request deleted successfully", null);
    }
}

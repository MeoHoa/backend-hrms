package com.example.hrms.controller.admin;

import com.example.hrms.dto.base.ApiResponse;
import com.example.hrms.dto.response.LeaveRequestResponse;
import com.example.hrms.dto.base.PageResponse;
import com.example.hrms.dto.request.RejectLeaveRequest;
import com.example.hrms.service.LeaveService;
import com.example.hrms.util.ResponseHelper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.hrms.entity.OnLeave;

@RestController
@RequestMapping("/api/admin/leave-requests")
@RequiredArgsConstructor
public class AdminLeaveController {
    private final LeaveService leaveService;

    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<PageResponse<LeaveRequestResponse>>> getPendingLeaveRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<LeaveRequestResponse> pendingRequests = leaveService.getPendingLeaveRequests(pageable);
        
        PageResponse<LeaveRequestResponse> pageResponse = PageResponse.<LeaveRequestResponse>builder()
                .total(pendingRequests.getTotalElements())
                .totalPages(pendingRequests.getTotalPages())
                .currentPage(pendingRequests.getNumber())
                .size(pendingRequests.getSize())
                .content(pendingRequests.getContent())
                .build();
        
        return ResponseHelper.success("Pending leave requests retrieved successfully", pageResponse);
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<PageResponse<LeaveRequestResponse>>> getAllLeaveRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) OnLeave.Status status,
            @RequestParam(required = false) Integer employeeId,
            @RequestParam(required = false) Integer leaveTypeId,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<LeaveRequestResponse> requests = leaveService.getAllLeaveRequests(
                status, employeeId, leaveTypeId, month, year, pageable);
        
        PageResponse<LeaveRequestResponse> pageResponse = PageResponse.<LeaveRequestResponse>builder()
                .total(requests.getTotalElements())
                .totalPages(requests.getTotalPages())
                .currentPage(requests.getNumber())
                .size(requests.getSize())
                .content(requests.getContent())
                .build();
        
        return ResponseHelper.success("Leave requests retrieved successfully", pageResponse);
    }

    @PutMapping("/approve/{requestId}")
    public ResponseEntity<ApiResponse<LeaveRequestResponse>> approveLeaveRequest(@PathVariable Integer requestId) {
        LeaveRequestResponse response = leaveService.approveLeaveRequest(requestId);
        return ResponseHelper.success("Leave request approved successfully", response);
    }

    @PutMapping("/reject/{requestId}")
    public ResponseEntity<ApiResponse<LeaveRequestResponse>> rejectLeaveRequest(
            @PathVariable Integer requestId,
            @Valid @RequestBody RejectLeaveRequest request) {
        LeaveRequestResponse response = leaveService.rejectLeaveRequest(requestId, request);
        return ResponseHelper.success("Leave request rejected successfully", response);
    }
}

package com.example.hrms.controller.admin;

import com.example.hrms.dto.base.ApiResponse;
import com.example.hrms.dto.request.ApproveTimekeepingRequest;
import com.example.hrms.dto.request.BatchApproveRequest;
import com.example.hrms.dto.request.BatchRejectRequest;
import com.example.hrms.dto.request.MarkErrorRequest;
import com.example.hrms.dto.base.PageResponse;
import com.example.hrms.dto.request.RejectTimekeepingRequest;
import com.example.hrms.dto.response.TimekeepingResponse;
import com.example.hrms.dto.response.TimekeepingStatsResponse;
import com.example.hrms.dto.request.UpdateTimekeepingRequest;
import com.example.hrms.entity.Timekeeping;
import com.example.hrms.service.TimekeepingService;
import com.example.hrms.util.ResponseHelper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin/timekeeping")
@RequiredArgsConstructor
public class AdminTimekeepingController {
    private final TimekeepingService timekeepingService;

    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<PageResponse<TimekeepingResponse>>> getPendingTimekeeping(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<TimekeepingResponse> pendingRecords = timekeepingService.getPendingTimekeeping(pageable);
        
        PageResponse<TimekeepingResponse> pageResponse = PageResponse.<TimekeepingResponse>builder()
                .total(pendingRecords.getTotalElements())
                .totalPages(pendingRecords.getTotalPages())
                .currentPage(pendingRecords.getNumber())
                .size(pendingRecords.getSize())
                .content(pendingRecords.getContent())
                .build();
        
        return ResponseHelper.success("Pending timekeeping records retrieved successfully", pageResponse);
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<PageResponse<TimekeepingResponse>>> getAllTimekeeping(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Timekeeping.Status status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate workDate,
            @RequestParam(required = false) Integer employeeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<TimekeepingResponse> records = timekeepingService.getAllTimekeeping(
                status, workDate, employeeId, startDate, endDate, pageable);
        
        PageResponse<TimekeepingResponse> pageResponse = PageResponse.<TimekeepingResponse>builder()
                .total(records.getTotalElements())
                .totalPages(records.getTotalPages())
                .currentPage(records.getNumber())
                .size(records.getSize())
                .content(records.getContent())
                .build();
        
        return ResponseHelper.success("Timekeeping records retrieved successfully", pageResponse);
    }

    @GetMapping("/{recordId}")
    public ResponseEntity<ApiResponse<TimekeepingResponse>> getTimekeepingById(@PathVariable Integer recordId) {
        TimekeepingResponse response = timekeepingService.getTimekeepingById(recordId);
        return ResponseHelper.success("Timekeeping record retrieved successfully", response);
    }

    @PutMapping("/approve/{recordId}")
    public ResponseEntity<ApiResponse<TimekeepingResponse>> approveTimekeeping(
            @PathVariable Integer recordId,
            @RequestBody(required = false) ApproveTimekeepingRequest request) {
        TimekeepingResponse response = timekeepingService.approveTimekeeping(recordId, request);
        return ResponseHelper.success("Timekeeping record approved successfully", response);
    }

    @PostMapping("/batch-approve")
    public ResponseEntity<ApiResponse<List<TimekeepingResponse>>> batchApproveTimekeeping(
            @Valid @RequestBody BatchApproveRequest request) {
        List<TimekeepingResponse> responses = timekeepingService.batchApproveTimekeeping(request);
        return ResponseHelper.success("Batch approval completed. " + responses.size() + " records approved", responses);
    }

    @PutMapping("/reject/{recordId}")
    public ResponseEntity<ApiResponse<TimekeepingResponse>> rejectTimekeeping(
            @PathVariable Integer recordId,
            @Valid @RequestBody RejectTimekeepingRequest request) {
        TimekeepingResponse response = timekeepingService.rejectTimekeeping(recordId, request);
        return ResponseHelper.success("Timekeeping record rejected successfully", response);
    }

    @PostMapping("/batch-reject")
    public ResponseEntity<ApiResponse<List<TimekeepingResponse>>> batchRejectTimekeeping(
            @Valid @RequestBody BatchRejectRequest request) {
        List<TimekeepingResponse> responses = timekeepingService.batchRejectTimekeeping(request);
        return ResponseHelper.success("Batch rejection completed. " + responses.size() + " records rejected", responses);
    }

    @PutMapping("/{recordId}")
    public ResponseEntity<ApiResponse<TimekeepingResponse>> updateTimekeeping(
            @PathVariable Integer recordId,
            @Valid @RequestBody UpdateTimekeepingRequest request) {
        TimekeepingResponse response = timekeepingService.updateTimekeeping(recordId, request);
        return ResponseHelper.success("Timekeeping record updated successfully", response);
    }

    @PutMapping("/{recordId}/mark-error")
    public ResponseEntity<ApiResponse<TimekeepingResponse>> markTimekeepingAsError(
            @PathVariable Integer recordId,
            @RequestBody(required = false) MarkErrorRequest request) {
        String reason = request != null ? request.getReason() : null;
        TimekeepingResponse response = timekeepingService.markTimekeepingAsError(recordId, reason);
        return ResponseHelper.success("Timekeeping record marked as error successfully", response);
    }

    @GetMapping("/error")
    public ResponseEntity<ApiResponse<PageResponse<TimekeepingResponse>>> getErrorTimekeeping(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<TimekeepingResponse> errorRecords = timekeepingService.getErrorTimekeeping(pageable);
        
        PageResponse<TimekeepingResponse> pageResponse = PageResponse.<TimekeepingResponse>builder()
                .total(errorRecords.getTotalElements())
                .totalPages(errorRecords.getTotalPages())
                .currentPage(errorRecords.getNumber())
                .size(errorRecords.getSize())
                .content(errorRecords.getContent())
                .build();
        
        return ResponseHelper.success("Error timekeeping records retrieved successfully", pageResponse);
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<TimekeepingStatsResponse>> getTimekeepingStats() {
        TimekeepingStatsResponse stats = timekeepingService.getTimekeepingStats();
        return ResponseHelper.success("Timekeeping statistics retrieved successfully", stats);
    }
}


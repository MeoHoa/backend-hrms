package com.example.hrms.controller;

import com.example.hrms.dto.base.ApiResponse;
import com.example.hrms.service.CategoryService;
import com.example.hrms.util.ResponseHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import com.example.hrms.dto.request.*;
import com.example.hrms.dto.response.*;
/**
 * Public category endpoints (accessible by both admin and employees)
 */
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryPublicController {
    private final CategoryService categoryService;

    @GetMapping("/leavetypes")
    public ResponseEntity<ApiResponse<List<LeaveTypeSimpleResponse>>> getActiveLeaveTypes() {
        List<LeaveTypeSimpleResponse> leaveTypes = categoryService.getActiveLeaveTypes();
        return ResponseHelper.success("Active leave types retrieved successfully", leaveTypes);
    }
}


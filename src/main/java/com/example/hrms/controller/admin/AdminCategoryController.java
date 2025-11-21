package com.example.hrms.controller.admin;

import com.example.hrms.dto.base.ApiResponse;
import com.example.hrms.dto.request.CreateDepartmentRequest;
import com.example.hrms.dto.response.DepartmentResponse;
import com.example.hrms.dto.response.LeaveTypeResponse;
import com.example.hrms.service.CategoryService;
import com.example.hrms.util.ResponseHelper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminCategoryController {
    private final CategoryService categoryService;

    @GetMapping("/departments")
    public ResponseEntity<ApiResponse<List<DepartmentResponse>>> getAllDepartments() {
        List<DepartmentResponse> departments = categoryService.getAllDepartments();
        return ResponseHelper.success("Departments retrieved successfully", departments);
    }

    @PostMapping("/departments")
    public ResponseEntity<ApiResponse<DepartmentResponse>> createDepartment(@Valid @RequestBody CreateDepartmentRequest request) {
        DepartmentResponse response = categoryService.createDepartment(request);
        return ResponseHelper.created("Department created successfully", response);
    }

    @GetMapping("/leave-types")
    public ResponseEntity<ApiResponse<List<LeaveTypeResponse>>> getAllLeaveTypes() {
        List<LeaveTypeResponse> leaveTypes = categoryService.getAllLeaveTypes();
        return ResponseHelper.success("Leave types retrieved successfully", leaveTypes);
    }

    @GetMapping("/shifts")
    public ResponseEntity<ApiResponse<List<Object>>> getAllShifts() {
        // Note: Shift entity not yet implemented in database
        // This endpoint is a placeholder for future implementation
        // For now, shifts are managed through WorkSchedule's fromHour and toHour fields
        return ResponseHelper.success("Shifts retrieved successfully", List.of());
    }
}

package com.example.hrms.controller.admin;

import com.example.hrms.dto.base.PageResponse;
import com.example.hrms.dto.request.CreateEmployeeRequest;
import com.example.hrms.dto.request.UpdateEmployeeRequest;
import com.example.hrms.dto.base.ApiResponse;
import com.example.hrms.dto.response.EmployeeResponse;
import com.example.hrms.dto.response.MessageResponse;
import com.example.hrms.service.EmployeeService;
import com.example.hrms.util.ResponseHelper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/employees")
@RequiredArgsConstructor
public class AdminEmployeeController {
    private final EmployeeService employeeService;

    @PostMapping
    public ResponseEntity<ApiResponse<EmployeeResponse>> createEmployee(@Valid @RequestBody CreateEmployeeRequest request) {
        EmployeeResponse response = employeeService.createEmployee(request);
        return ResponseHelper.created("Employee created successfully", response);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<EmployeeResponse>>> getAllEmployees(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer departmentId) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<EmployeeResponse> employees = employeeService.getAllEmployees(search, departmentId, pageable);
        
        PageResponse<EmployeeResponse> pageResponse = PageResponse.<EmployeeResponse>builder()
                .total(employees.getTotalElements())
                .totalPages(employees.getTotalPages())
                .currentPage(employees.getNumber())
                .size(employees.getSize())
                .content(employees.getContent())
                .build();
        
        return ResponseHelper.success("Employees retrieved successfully", pageResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EmployeeResponse>> getEmployeeById(@PathVariable Integer id) {
        EmployeeResponse response = employeeService.getEmployeeById(id);
        return ResponseHelper.success("Employee retrieved successfully", response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EmployeeResponse>> updateEmployee(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateEmployeeRequest request) {
        EmployeeResponse response = employeeService.updateEmployee(id, request);
        return ResponseHelper.success("Employee updated successfully", response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<MessageResponse>> deleteEmployee(@PathVariable Integer id) {
        employeeService.deleteEmployee(id);
        MessageResponse message = MessageResponse.builder()
                .message("Employee disabled successfully")
                .build();
        return ResponseHelper.success("Employee disabled successfully", message);
    }

    @GetMapping("/my-department")
    public ResponseEntity<ApiResponse<PageResponse<EmployeeResponse>>> getMyDepartmentEmployees(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<EmployeeResponse> employees = employeeService.getMyDepartmentEmployees(search, pageable);
        
        PageResponse<EmployeeResponse> pageResponse = PageResponse.<EmployeeResponse>builder()
                .total(employees.getTotalElements())
                .totalPages(employees.getTotalPages())
                .currentPage(employees.getNumber())
                .size(employees.getSize())
                .content(employees.getContent())
                .build();
        
        return ResponseHelper.success("Department employees retrieved successfully", pageResponse);
    }
}

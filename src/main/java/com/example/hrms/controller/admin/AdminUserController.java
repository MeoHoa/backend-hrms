package com.example.hrms.controller.admin;

import com.example.hrms.dto.base.PageResponse;
import com.example.hrms.dto.request.AssignRoleRequest;
import com.example.hrms.dto.request.CreateUserRequest;
import com.example.hrms.dto.request.ResetPasswordRequest;
import com.example.hrms.dto.request.UpdateUserRequest;
import com.example.hrms.dto.base.ApiResponse;
import com.example.hrms.dto.response.MessageResponse;
import com.example.hrms.dto.response.UserResponse;
import com.example.hrms.service.UserManagementService;
import com.example.hrms.util.ResponseHelper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {
    private final UserManagementService userManagementService;

    @GetMapping("/getAllUsers")
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer roleId) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<UserResponse> users = userManagementService.getAllUsers(search, roleId, pageable);
        
        PageResponse<UserResponse> pageResponse = PageResponse.<UserResponse>builder()
                .total(users.getTotalElements())
                .totalPages(users.getTotalPages())
                .currentPage(users.getNumber())
                .size(users.getSize())
                .content(users.getContent())
                .build();
        
        return ResponseHelper.success("Users retrieved successfully", pageResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Integer id) {
        UserResponse response = userManagementService.getUserById(id);
        return ResponseHelper.success("User retrieved successfully", response);
    }

    @PostMapping("/createUser")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserResponse response = userManagementService.createUser(request);
        return ResponseHelper.created("User created successfully", response);
    }

    @PutMapping("/updateUser/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateUserRequest request) {
        UserResponse response = userManagementService.updateUser(id, request);
        return ResponseHelper.success("User updated successfully", response);
    }

    @PutMapping("/{id}/assign-role")
    public ResponseEntity<ApiResponse<UserResponse>> assignRole(
            @PathVariable Integer id,
            @Valid @RequestBody AssignRoleRequest request) {
        UserResponse response = userManagementService.assignRole(id, request);
        return ResponseHelper.success("Role assigned successfully", response);
    }

    @PutMapping("/{id}/reset-password")
    public ResponseEntity<ApiResponse<MessageResponse>> resetPassword(
            @PathVariable Integer id,
            @Valid @RequestBody ResetPasswordRequest request) {
        userManagementService.resetPassword(id, request);
        MessageResponse message = MessageResponse.builder()
                .message("Password reset successfully")
                .build();
        return ResponseHelper.success("Password reset successfully", message);
    }

    @DeleteMapping("/deleteUser/{id}")
    public ResponseEntity<ApiResponse<MessageResponse>> deleteUser(@PathVariable Integer id) {
        userManagementService.deleteUser(id);
        MessageResponse message = MessageResponse.builder()
                .message("User deleted successfully")
                .build();
        return ResponseHelper.success("User deleted successfully", message);
    }
}


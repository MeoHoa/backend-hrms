package com.example.hrms.controller.admin;

import com.example.hrms.dto.base.PageResponse;
import com.example.hrms.dto.request.CreateRoleRequest;
import com.example.hrms.dto.request.UpdateRoleRequest;
import com.example.hrms.dto.base.ApiResponse;
import com.example.hrms.dto.response.MessageResponse;
import com.example.hrms.dto.response.RoleResponse;
import com.example.hrms.service.RoleService;
import com.example.hrms.util.ResponseHelper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/roles")
@RequiredArgsConstructor
public class AdminRoleController {
    private final RoleService roleService;

    @GetMapping
    public ResponseEntity<ApiResponse<Object>> getAllRoles(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false, defaultValue = "false") boolean all) {
        
        if (all) {
            // Return all roles as list (for dropdowns)
            List<RoleResponse> roles = roleService.getAllRoles();
            return ResponseHelper.success("Roles retrieved successfully", roles);
        } else {
            // Return paginated result
            Pageable pageable = PageRequest.of(page, size);
            Page<RoleResponse> roles = roleService.getAllRoles(search, pageable);
            
            PageResponse<RoleResponse> pageResponse = PageResponse.<RoleResponse>builder()
                    .total(roles.getTotalElements())
                    .totalPages(roles.getTotalPages())
                    .currentPage(roles.getNumber())
                    .size(roles.getSize())
                    .content(roles.getContent())
                    .build();
            
            return ResponseHelper.success("Roles retrieved successfully", pageResponse);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RoleResponse>> getRoleById(@PathVariable Integer id) {
        RoleResponse response = roleService.getRoleById(id);
        return ResponseHelper.success("Role retrieved successfully", response);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<RoleResponse>> createRole(@Valid @RequestBody CreateRoleRequest request) {
        RoleResponse response = roleService.createRole(request);
        return ResponseHelper.created("Role created successfully", response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RoleResponse>> updateRole(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateRoleRequest request) {
        RoleResponse response = roleService.updateRole(id, request);
        return ResponseHelper.success("Role updated successfully", response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<MessageResponse>> deleteRole(@PathVariable Integer id) {
        roleService.deleteRole(id);
        MessageResponse message = MessageResponse.builder()
                .message("Role deleted successfully")
                .build();
        return ResponseHelper.success("Role deleted successfully", message);
    }
}


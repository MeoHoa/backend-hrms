package com.example.hrms.service;

import com.example.hrms.entity.Role;
import com.example.hrms.repository.RoleRepository;
import com.example.hrms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import com.example.hrms.dto.request.*;
import com.example.hrms.dto.response.*;
@Service
@RequiredArgsConstructor
public class RoleService {
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    public List<RoleResponse> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public Page<RoleResponse> getAllRoles(String search, Pageable pageable) {
        Page<Role> roles = roleRepository.searchRoles(search, pageable);
        return roles.map(this::mapToResponse);
    }

    public RoleResponse getRoleById(Integer id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found with id: " + id));
        return mapToResponse(role);
    }

    @Transactional
    public RoleResponse createRole(CreateRoleRequest request) {
        // Check if role key already exists
        if (roleRepository.findByRoleKey(request.getRoleKey()).isPresent()) {
            throw new RuntimeException("Role key already exists: " + request.getRoleKey());
        }

        Role role = Role.builder()
                .roleKey(request.getRoleKey())
                .roleName(request.getRoleName())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        role = roleRepository.save(role);
        return mapToResponse(role);
    }

    @Transactional
    public RoleResponse updateRole(Integer id, UpdateRoleRequest request) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found with id: " + id));

        if (request.getRoleKey() != null && !request.getRoleKey().equals(role.getRoleKey())) {
            // Check if new role key already exists
            if (roleRepository.findByRoleKey(request.getRoleKey()).isPresent()) {
                throw new RuntimeException("Role key already exists: " + request.getRoleKey());
            }
            role.setRoleKey(request.getRoleKey());
        }

        if (request.getRoleName() != null) {
            role.setRoleName(request.getRoleName());
        }

        role.setUpdatedAt(LocalDateTime.now());
        role = roleRepository.save(role);

        return mapToResponse(role);
    }

    @Transactional
    public void deleteRole(Integer id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found with id: " + id));

        // Check if role is being used by any users
        long userCount = userRepository.findByRole(role, Pageable.unpaged()).getTotalElements();
        if (userCount > 0) {
            throw new RuntimeException("Cannot delete role. There are " + userCount + " user(s) assigned to this role.");
        }

        roleRepository.delete(role);
    }

    private RoleResponse mapToResponse(Role role) {
        return RoleResponse.builder()
                .roleId(role.getRoleId())
                .roleKey(role.getRoleKey())
                .roleName(role.getRoleName())
                .createdAt(role.getCreatedAt())
                .updatedAt(role.getUpdatedAt())
                .build();
    }
}


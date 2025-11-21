package com.example.hrms.service;
import com.example.hrms.dto.request.*;
import com.example.hrms.dto.response.*;
import com.example.hrms.entity.Employee;
import com.example.hrms.entity.Role;
import com.example.hrms.entity.User;
import com.example.hrms.repository.EmployeeRepository;
import com.example.hrms.repository.RoleRepository;
import com.example.hrms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserManagementService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    public Page<UserResponse> getAllUsers(String search, Integer roleId, Pageable pageable) {
        Page<User> users = userRepository.searchUsers(search, roleId, pageable);
        return users.map(this::mapToResponse);
    }

    public UserResponse getUserById(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        return mapToResponse(user);
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        // Check if username already exists
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists: " + request.getUsername());
        }
        
        // Check if email already exists
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists: " + request.getEmail());
        }

        // Get role
        Role role = null;
        if (request.getRoleId() != null) {
            role = roleRepository.findById(request.getRoleId())
                    .orElseThrow(() -> new RuntimeException("Role not found with id: " + request.getRoleId()));
        } else {
            // Default to EMPLOYEE role
            role = roleRepository.findByRoleKey("EMPLOYEE")
                    .orElseThrow(() -> new RuntimeException("Default EMPLOYEE role not found"));
        }

        // Get employee if provided
        Employee employee = null;
        if (request.getEmployeeId() != null) {
            employee = employeeRepository.findById(request.getEmployeeId())
                    .orElseThrow(() -> new RuntimeException("Employee not found with id: " + request.getEmployeeId()));
            
            // Check if employee already has a user account
            if (userRepository.findByEmployee(employee).isPresent()) {
                throw new RuntimeException("Employee already has a user account");
            }
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .avatarData(request.getAvatarBase64())
                .role(role)
                .employee(employee)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        user = userRepository.save(user);
        return mapToResponse(user);
    }

    @Transactional
    public UserResponse updateUser(Integer id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        if (request.getUsername() != null && !request.getUsername().equals(user.getUsername())) {
            // Check if username already exists
            if (userRepository.findByUsername(request.getUsername()).isPresent()) {
                throw new RuntimeException("Username already exists: " + request.getUsername());
            }
            user.setUsername(request.getUsername());
        }

        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            // Check if email already exists
            if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                throw new RuntimeException("Email already exists: " + request.getEmail());
            }
            user.setEmail(request.getEmail());
        }

        if (request.getRoleId() != null) {
            Role role = roleRepository.findById(request.getRoleId())
                    .orElseThrow(() -> new RuntimeException("Role not found with id: " + request.getRoleId()));
            user.setRole(role);
        }

        if (request.getEmployeeId() != null) {
            Employee employee = employeeRepository.findById(request.getEmployeeId())
                    .orElseThrow(() -> new RuntimeException("Employee not found with id: " + request.getEmployeeId()));
            
            // Check if employee is already assigned to another user
            User finalUser = user;
            userRepository.findByEmployee(employee).ifPresent(existingUser -> {
                if (!existingUser.getUserId().equals(finalUser.getUserId())) {
                    throw new RuntimeException("Employee is already assigned to another user");
                }
            });
            user.setEmployee(employee);
        }

        if (request.getAvatarBase64() != null) {
            user.setAvatarData(request.getAvatarBase64());
        }

        user.setUpdatedAt(LocalDateTime.now());
        user = userRepository.save(user);

        return mapToResponse(user);
    }

    @Transactional
    public UserResponse assignRole(Integer userId, AssignRoleRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new RuntimeException("Role not found with id: " + request.getRoleId()));

        user.setRole(role);
        user.setUpdatedAt(LocalDateTime.now());
        user = userRepository.save(user);

        return mapToResponse(user);
    }

    @Transactional
    public void resetPassword(Integer userId, ResetPasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        if (request.getNewPassword().length() < 6) {
            throw new RuntimeException("Password must be at least 6 characters");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        // Soft delete: just remove the employee link and clear some fields
        // Or we can add an active field to User entity in the future
        userRepository.delete(user);
    }

    private UserResponse mapToResponse(User user) {
        UserResponse.UserResponseBuilder builder = UserResponse.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .active(true) // Default to true, can be enhanced with status field later
                .avatarBase64(user.getAvatarData())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt());

        if (user.getRole() != null) {
            builder.roleId(user.getRole().getRoleId())
                    .roleKey(user.getRole().getRoleKey())
                    .roleName(user.getRole().getRoleName());
        }

        if (user.getEmployee() != null) {
            builder.employeeId(user.getEmployee().getEmployeeId());
        }

        return builder.build();
    }
}


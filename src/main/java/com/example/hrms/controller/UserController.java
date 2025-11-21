package com.example.hrms.controller;

import com.example.hrms.dto.base.ApiResponse;
import com.example.hrms.entity.User;
import com.example.hrms.repository.EmployeeRepository;
import com.example.hrms.repository.UserRepository;
import com.example.hrms.service.EmployeeService;
import com.example.hrms.util.ResponseHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import com.example.hrms.dto.request.*;
import com.example.hrms.dto.response.*;
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmployeeService employeeService;

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
    }

    @GetMapping("/my-employee")
    public ResponseEntity<ApiResponse<MyEmployeeProfileResponse>> getMyEmployee() {
        User currentUser = getCurrentUser();
        MyEmployeeProfileResponse response = employeeService.getMyEmployeeProfile(currentUser);
        return ResponseHelper.success("Employee profile retrieved successfully", response);
    }

    @PutMapping("/my-employee")
    @Transactional
    public ResponseEntity<ApiResponse<MyEmployeeProfileResponse>> updateMyEmployee(
            @RequestBody UpdateMyEmployeeProfileRequest request) {
        User currentUser = getCurrentUser();
        MyEmployeeProfileResponse response = employeeService.updateMyEmployeeProfile(currentUser, request);
        return ResponseHelper.success("Employee profile updated successfully", response);
    }

    @PostMapping("/change-password")
    @Transactional
    public ResponseEntity<ApiResponse<MessageResponse>> changePassword(@RequestBody java.util.Map<String, String> body) {
        User currentUser = getCurrentUser();
        
        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");
        
        if (oldPassword == null || newPassword == null) {
            return ResponseHelper.badRequest("Old password and new password are required");
        }
        
        if (!passwordEncoder.matches(oldPassword, currentUser.getPassword())) {
            return ResponseHelper.badRequest("Old password is incorrect");
        }
        
        if (newPassword.length() < 6) {
            return ResponseHelper.badRequest("New password must be at least 6 characters");
        }
        
        currentUser.setPassword(passwordEncoder.encode(newPassword));
        // Reset mustChangePassword flag when user changes password
        currentUser.setStatus(1);
        userRepository.save(currentUser);
        
        MessageResponse message = MessageResponse.builder()
                .message("Password changed successfully")
                .build();
        return ResponseHelper.success("Password changed successfully", message);
    }

    @PutMapping("/my-profile")
    @Transactional
    public ResponseEntity<ApiResponse<UserInfoResponse>> updateProfile(@RequestBody java.util.Map<String, String> body) {
        User currentUser = getCurrentUser();
        
        if (body.containsKey("username") && body.get("username") != null && !body.get("username").equals(currentUser.getUsername())) {
            String newUsername = body.get("username");
            // Check if username already exists
            if (userRepository.findByUsername(newUsername).isPresent()) {
                return ResponseHelper.badRequest("Username already exists");
            }
            currentUser.setUsername(newUsername);
        }
        
        if (body.containsKey("email") && body.get("email") != null && !body.get("email").equals(currentUser.getEmail())) {
            String newEmail = body.get("email");
            // Check if email already exists
            if (userRepository.findByEmail(newEmail).isPresent()) {
                return ResponseHelper.badRequest("Email already exists");
            }
            currentUser.setEmail(newEmail);
        }
        
        if (body.containsKey("phoneNumber") && currentUser.getEmployee() != null) {
            String phoneNumber = body.get("phoneNumber");
            // Check if phone already exists for another employee
            if (phoneNumber != null && !phoneNumber.isEmpty()) {
                employeeRepository.findByPhone(phoneNumber).ifPresent(employee -> {
                    if (!employee.getEmployeeId().equals(currentUser.getEmployee().getEmployeeId())) {
                        throw new RuntimeException("Phone number already exists");
                    }
                });
            }
            currentUser.getEmployee().setPhone(phoneNumber);
            employeeRepository.save(currentUser.getEmployee());
        }
        
        userRepository.save(currentUser);
        
        UserInfoResponse profileInfo = UserInfoResponse.builder()
                .id(currentUser.getUserId())
                .username(currentUser.getUsername())
                .email(currentUser.getEmail())
                .role(currentUser.getRole() == null ? "" : currentUser.getRole().getRoleKey())
                .employeeId(currentUser.getEmployee() == null ? null : currentUser.getEmployee().getEmployeeId())
                .build();
        
        return ResponseHelper.success("Profile updated successfully", profileInfo);
    }

    @PostMapping("/update-avatar")
    @Transactional
    public ResponseEntity<ApiResponse<MessageResponse>> updateAvatar(
            @Valid @RequestBody UpdateAvatarRequest request) {
        User currentUser = getCurrentUser();
        
        // Validate base64 string format (basic check)
        if (request.getAvatarBase64() == null || request.getAvatarBase64().trim().isEmpty()) {
            return ResponseHelper.badRequest("Avatar base64 is required");
        }
        
        // Optional: Validate base64 format (should start with data:image/...;base64,)
        String base64Data = request.getAvatarBase64();
        if (!base64Data.startsWith("data:image/") && !base64Data.contains("base64,")) {
            // If it's just the base64 string without prefix, we can still accept it
            // But if it has prefix, validate format
            if (base64Data.contains("data:") && !base64Data.contains("base64,")) {
                return ResponseHelper.badRequest("Invalid base64 format. Expected format: data:image/[type];base64,[data]");
            }
        }
        
        // Update avatar
        currentUser.setAvatarData(base64Data);
        currentUser.setUpdatedAt(java.time.LocalDateTime.now());
        userRepository.save(currentUser);
        
        MessageResponse message = MessageResponse.builder()
                .message("Avatar updated successfully")
                .build();
        return ResponseHelper.success("Avatar updated successfully", message);
    }
}

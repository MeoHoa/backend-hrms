package com.example.hrms.controller;

import com.example.hrms.dto.base.ApiResponse;
import com.example.hrms.service.AuthService;
import com.example.hrms.repository.UserRepository;
import com.example.hrms.util.ResponseHelper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import com.example.hrms.dto.request.*;
import com.example.hrms.dto.response.*;
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final UserRepository userRepository;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody AuthRequest req) {
        AuthResponse resp = authService.login(req);
        return ResponseHelper.success("Login successful", resp);
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserInfoResponse>> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() ||
                authentication.getName() == null || authentication.getName().equals("anonymousUser")) {
            return ResponseHelper.unauthorized("User not authenticated");
        }

        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .map(user -> {
                    UserInfoResponse userInfo = UserInfoResponse.builder()
                            .id(user.getUserId())
                            .username(user.getUsername())
                            .email(user.getEmail())
                            .role(user.getRole() == null ? "" : user.getRole().getRoleKey())
                            .employeeId(user.getEmployee() == null ? null : user.getEmployee().getEmployeeId())
                            .status(user.getStatus())
                            .build();
                    return ResponseHelper.success("User information retrieved successfully", userInfo);
                })
                .orElseGet(() -> ResponseHelper.notFound("User not found"));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<MessageResponse>> logout() {
        // With JWT, logout is typically handled client-side by removing the token
        // If using token blacklisting, you would invalidate the token here
        SecurityContextHolder.clearContext();
        MessageResponse message = MessageResponse.builder()
                .message("Logged out successfully")
                .build();
        return ResponseHelper.success("Logout successful", message);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<MessageResponse>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        MessageResponse message = MessageResponse.builder()
                .message("Password reset link has been sent to your email")
                .build();
        return ResponseHelper.success("Password reset email sent successfully", message);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<MessageResponse>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        MessageResponse message = MessageResponse.builder()
                .message("Password has been reset successfully")
                .build();
        return ResponseHelper.success("Password reset successfully", message);
    }
}

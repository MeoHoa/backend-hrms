package com.example.hrms.service;

import com.example.hrms.dto.UserInfo;
import com.example.hrms.dto.request.*;
import com.example.hrms.dto.response.*;
import com.example.hrms.entity.User;
import com.example.hrms.exception.UserNotFoundException;
import com.example.hrms.repository.UserRepository;
import com.example.hrms.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    public AuthResponse login(AuthRequest req) {
        // Check if user exists first (can be username or email)
        User user = userRepository.findByUsernameOrEmail(req.getUsername())
                .orElseThrow(() -> new UserNotFoundException("User not found with username or email: " + req.getUsername()));
        
        // If user exists, try to authenticate (will check password)
        // Use email as principal for Spring Security authentication
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getEmail(), req.getPassword())
            );
        } catch (BadCredentialsException | UsernameNotFoundException e) {
            // This will catch password mismatch or other authentication issues
            throw new BadCredentialsException("Invalid password");
        }
        
        // Check if user must change password
        // Note: This information should be returned to frontend to prompt password change
        // But we still allow login to proceed
        
        // Authentication successful, generate token (use email for token)
        String token = jwtUtil.generateToken(user.getEmail());
        UserInfo userInfo = UserInfo.builder()
                .id(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole() == null ? null : user.getRole().getRoleKey())
                .status(user.getStatus())
                .avatarBase64(user.getAvatarData())
                .build();
        
        return AuthResponse.builder()
                .token(token)
                .user(userInfo)
                .build();
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + request.getEmail()));

        // Generate reset token
        String resetToken = UUID.randomUUID().toString();
        LocalDateTime expiryTime = LocalDateTime.now().plusHours(1); // Token expires in 1 hour

        // Save reset token to user
        user.setResetToken(resetToken);
        user.setResetTokenExpiry(expiryTime);
        userRepository.save(user);

        // Get employee name for email
        String fullName = user.getEmployee() != null ? user.getEmployee().getFullName() : user.getUsername();

        // Send password reset email
        emailService.sendPasswordResetEmail(user.getEmail(), fullName, resetToken);
        
        log.info("Password reset token generated for user: {}", user.getEmail());
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        // Find user by reset token
        User user = userRepository.findByResetToken(request.getToken())
                .filter(u -> u.getResetTokenExpiry() != null && u.getResetTokenExpiry().isAfter(LocalDateTime.now()))
                .orElseThrow(() -> new RuntimeException("Invalid or expired reset token"));

        // Update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        user.setStatus(1); // Set status to active after password reset
        userRepository.save(user);

        log.info("Password reset successfully for user: {}", user.getEmail());
    }
}

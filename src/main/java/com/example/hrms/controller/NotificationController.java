package com.example.hrms.controller;

import com.example.hrms.dto.base.ApiResponse;
import com.example.hrms.dto.response.NotificationResponse;
import com.example.hrms.service.NotificationService;
import com.example.hrms.util.ResponseHelper;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getNotifications(
            @RequestParam(defaultValue = "employee") String scope,
            @RequestParam(defaultValue = "10") @Min(1) Integer limit) {
        if (!"employee".equalsIgnoreCase(scope)) {
            throw new RuntimeException("Unsupported scope: " + scope + ". Only 'employee' is supported.");
        }
        int resolvedLimit = Math.min(limit, 50);
        List<NotificationResponse> notifications = notificationService.getNotificationsForCurrentUser(resolvedLimit);
        return ResponseHelper.success("Notifications fetched successfully", notifications);
    }
}


package com.example.hrms.service;

import com.example.hrms.dto.response.NotificationResponse;
import com.example.hrms.entity.Notification;
import com.example.hrms.entity.User;
import com.example.hrms.repository.NotificationRepository;
import com.example.hrms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
    }

    public List<NotificationResponse> getNotificationsForCurrentUser(int limit) {
        User user = getCurrentUser();
        List<Notification> notifications = notificationRepository.findByUserOrderByCreatedAtDesc(
                user,
                PageRequest.of(0, Math.max(1, limit))
        );

        return notifications.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private NotificationResponse mapToResponse(Notification notification) {
        return NotificationResponse.builder()
                .notificationId(notification.getNotificationId())
                .message(notification.getMessage())
                .status(notification.getStatus() != null ? notification.getStatus().name() : null)
                .link(notification.getLink())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}


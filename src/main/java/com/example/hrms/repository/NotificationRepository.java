package com.example.hrms.repository;

import com.example.hrms.entity.Notification;
import com.example.hrms.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    List<Notification> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
}

package com.example.hrms.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "holiday_calendar", 
       uniqueConstraints = @UniqueConstraint(columnNames = "holiday_date", name = "uk_holiday_date"))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HolidayCalendar {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer holidayId;

    @Column(name = "holiday_name", nullable = false, length = 255)
    private String holidayName;

    @Column(name = "holiday_date", nullable = false, unique = true)
    private LocalDate holidayDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}


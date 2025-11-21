
package com.example.hrms.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.math.BigDecimal;

@Entity
@Table(name = "timekeeping", uniqueConstraints = {@UniqueConstraint(columnNames = {"employee_id","work_date"}, name = "uk_employee_workdate")})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Timekeeping {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne @JoinColumn(name = "employee_id")
    private Employee employee;

    private LocalDateTime checkIn;
    private LocalDateTime checkOut;
    private LocalDate workDate;
    
    // Expected working hours (from schedule, default 8 hours)
    private BigDecimal requiredWorkHours;
    
    // Actual calculated work hours
    private BigDecimal workHours;
    private BigDecimal overtimeHours;
    
    // Expected check-in/check-out times (from schedule)
    private LocalTime expectedCheckInTime;
    private LocalTime expectedCheckOutTime;

    // Reason for abnormal timekeeping (UI will control and send: late, early, missing, insufficient)
    // Examples: "Check-in muộn: Kẹt xe", "Check-out sớm: Việc gia đình", "Quên chấm công", "Thiếu giờ làm"
    @Column(columnDefinition = "TEXT")
    private String reason;

    // Admin notes when correcting/approving
    @Column(columnDefinition = "TEXT")
    private String adminNote;

    // Holiday name if workDate is a holiday (e.g., "Tết Dương lịch", "Giải phóng miền Nam")
    // Null if workDate is not a holiday
    private String holidayName;

    @Enumerated(EnumType.STRING)
    private Status status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum Status { Pending, Confirmed, Error }
}

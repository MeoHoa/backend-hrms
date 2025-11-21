
package com.example.hrms.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "onleave")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OnLeave {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer requestId;

    @ManyToOne @JoinColumn(name = "employee_id")
    private Employee employee;

    @ManyToOne @JoinColumn(name = "leavetype_id")
    private LeaveType leaveType;

    @Column(columnDefinition = "TEXT")
    private String reason;

    private LocalDate fromDate;
    private LocalDate toDate;

    @Enumerated(EnumType.STRING)
    private Status status;

    @ManyToOne @JoinColumn(name = "admin_id")
    private User admin;

    private LocalDateTime processedDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum Status { Pending, Approved, Rejected }
}

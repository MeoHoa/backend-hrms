package com.example.hrms.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "workschedule")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkSchedule {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer workId;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private LocalDate workDate;
    private LocalTime fromHour;
    private LocalTime toHour;

    // Phòng họp (meeting room)
    private String meetingRoom;

    // Trạng thái lịch làm việc
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Status status = Status.Active;

    // Host department (phòng ban chủ trì)
    @ManyToOne
    @JoinColumn(name = "host_department_id")
    private Department hostDepartment;

    // Host employee (người chủ trì)
    @ManyToOne
    @JoinColumn(name = "host_employee_id")
    private Employee hostEmployee;

    // Many-to-many với Department (các phòng ban tham gia)
    @OneToMany(mappedBy = "workSchedule", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<WorkScheduleDepartment> departments = new ArrayList<>();

    // Many-to-many với Employee (các nhân viên tham gia)
    @OneToMany(mappedBy = "workSchedule", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<WorkScheduleEmployee> employees = new ArrayList<>();

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum Status {
        Active,      // Đang hoạt động (sắp diễn ra hoặc đang diễn ra)
        Cancelled,   // Đã hủy
        Postponed,   // Đã hoãn
        Completed    // Đã hoàn thành
    }
}

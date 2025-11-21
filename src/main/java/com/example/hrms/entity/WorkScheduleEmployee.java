package com.example.hrms.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "work_schedule_employee")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkScheduleEmployee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "work_schedule_id", nullable = false)
    private WorkSchedule workSchedule;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;
}


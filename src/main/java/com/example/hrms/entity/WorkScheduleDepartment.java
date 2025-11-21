package com.example.hrms.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "work_schedule_department")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkScheduleDepartment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "work_schedule_id", nullable = false)
    private WorkSchedule workSchedule;

    @ManyToOne
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;
}


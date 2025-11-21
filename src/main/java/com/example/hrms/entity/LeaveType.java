
package com.example.hrms.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "leavetype")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveType {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer leavetypeId;

    private String leaveName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    private Status status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum Status { Active, Inactive }
}

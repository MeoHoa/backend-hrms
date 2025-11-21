package com.example.hrms.dto.response;

import com.example.hrms.entity.LeaveType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveTypeResponse {
    private Integer leavetypeId;
    private String leaveName;
    private String description;
    private LeaveType.Status status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}


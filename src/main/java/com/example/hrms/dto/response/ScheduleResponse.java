package com.example.hrms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleResponse {
    private Integer workId;
    private String title;
    private String description;
    private LocalDate workDate;
    private LocalTime fromHour;
    private LocalTime toHour;

    // Phòng họp (meeting room)
    private String meetingRoom;

    // Trạng thái lịch làm việc
    private String status;

    // Phòng ban chủ trì
    private Integer hostDepartmentId;
    private String hostDepartmentName;

    // Người chủ trì
    private Integer hostEmployeeId;
    private String hostEmployeeFullName;
    private String hostEmployeeEmail;

    // Danh sách các phòng ban tham gia
    private List<DepartmentInfo> departments;

    // Danh sách các nhân viên tham gia
    private List<EmployeeInfo> employees;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Inner classes cho thông tin phòng ban và nhân viên
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DepartmentInfo {
        private Integer departmentId;
        private String departmentName;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EmployeeInfo {
        private Integer employeeId;
        private String employeeFullName;
        private String employeeEmail;
    }
}


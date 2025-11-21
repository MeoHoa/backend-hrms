package com.example.hrms.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
public class CreateScheduleRequest {
    @NotNull(message = "Date is required")
    private LocalDate date;

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotNull(message = "From hour is required")
    private LocalTime fromHour;

    @NotNull(message = "To hour is required")
    private LocalTime toHour;

    // Phòng họp (meeting room) - tùy chọn
    private String meetingRoom;

    // Trạng thái lịch làm việc - tùy chọn (mặc định: Active)
    private String status;

    // Phòng ban chủ trì
    @NotNull(message = "Host department ID is required")
    private Integer hostDepartmentId;

    // Người chủ trì
    @NotNull(message = "Host employee ID is required")
    private Integer hostEmployeeId;

    // Danh sách các phòng ban tham gia (có thể nhiều)
    private List<Integer> departmentIds;

    // Danh sách các nhân viên tham gia (có thể nhiều)
    private List<Integer> employeeIds;

    // Deprecated fields - giữ lại để backward compatible
    @Deprecated
    private Integer employeeId;

    @Deprecated
    private Integer shiftId; // Optional - for future use if Shift entity is added

    @Deprecated
    private Integer departmentId; // Optional - can be derived from employee
}


package com.example.hrms.dto.request;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
public class UpdateScheduleRequest {
    // Tất cả các field đều optional - chỉ cập nhật những field được truyền vào
    
    private LocalDate date;
    
    private String title;
    
    private String description;
    
    private LocalTime fromHour;
    
    private LocalTime toHour;
    
    // Phòng họp (meeting room)
    private String meetingRoom;
    
    // Trạng thái lịch làm việc
    private String status;
    
    // Phòng ban chủ trì
    private Integer hostDepartmentId;
    
    // Người chủ trì
    private Integer hostEmployeeId;
    
    // Danh sách các phòng ban tham gia (có thể nhiều)
    // Nếu truyền vào, sẽ thay thế toàn bộ danh sách cũ
    private List<Integer> departmentIds;
    
    // Danh sách các nhân viên tham gia (có thể nhiều)
    // Nếu truyền vào, sẽ thay thế toàn bộ danh sách cũ
    private List<Integer> employeeIds;
}


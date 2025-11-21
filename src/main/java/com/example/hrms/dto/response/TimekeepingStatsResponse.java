package com.example.hrms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for timekeeping statistics
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimekeepingStatsResponse {
    private Long totalPending;    // Tổng số bản ghi chờ duyệt
    private Long totalConfirmed;  // Tổng số bản ghi đã duyệt
    private Long totalError;      // Tổng số bản ghi lỗi
    private Long totalRecords;    // Tổng số bản ghi
}


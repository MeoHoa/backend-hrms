package com.example.hrms.dto.request;

import lombok.Data;

/**
 * DTO for marking timekeeping record as error
 */
@Data
public class MarkErrorRequest {
    private String reason; // Lý do đánh dấu lỗi
}


package com.example.hrms.dto.request;

import lombok.Data;

/**
 * DTO for check-out request with optional reason
 * UI will control and send reason if needed (e.g., "Check-out sớm: Việc gia đình")
 */
@Data
public class CheckOutRequest {
    private String reason; // Lý do bất thường (nếu có) - UI sẽ quyết định
}


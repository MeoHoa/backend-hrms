package com.example.hrms.dto.request;

import lombok.Data;

/**
 * DTO for check-in request with optional reason
 * UI will control and send reason if needed (e.g., "Check-in muộn: Kẹt xe")
 */
@Data
public class CheckInRequest {
    private String reason; // Lý do bất thường (nếu có) - UI sẽ quyết định
}


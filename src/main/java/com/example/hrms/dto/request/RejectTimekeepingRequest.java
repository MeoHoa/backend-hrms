package com.example.hrms.dto.request;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RejectTimekeepingRequest {
    private String reason;
    private LocalDateTime correctedTimeIn;
    private LocalDateTime correctedTimeOut;
}


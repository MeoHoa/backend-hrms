package com.example.hrms.dto.request;

import lombok.Data;

import java.time.LocalDate;

/**
 * DTO for updating employee profile by regular users.
 * Only contains fields that users are allowed to modify.
 */
@Data
public class UpdateMyEmployeeProfileRequest {
    private String fullName;
    private LocalDate dateOfBirth;
    private String gender; // "Male", "Female", "Other"
    private String address;
    private String phone;
    private String emergencyContact;
    private String emergencyPhone;
    private String notes;
}


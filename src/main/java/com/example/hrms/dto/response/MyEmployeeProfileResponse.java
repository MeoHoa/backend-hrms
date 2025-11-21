package com.example.hrms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO for employee profile information that regular users can view and update.
 * Only contains fields that users are allowed to modify.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MyEmployeeProfileResponse {
    // Read-only fields (for display only)
    private Integer employeeId;
    private String email; // Read-only, email is managed by admin
    private String position; // Read-only
    private Integer departmentId;
    private String departmentName;
    private LocalDate hireDate; // Read-only
    private Integer userId;
    private String username;
    private String roleKey;
    
    // Editable fields by user
    private String fullName;
    private LocalDate dateOfBirth;
    private String gender;
    private String address;
    private String phone;
    private String emergencyContact;
    private String emergencyPhone;
    private String notes;
}


package com.example.hrms.dto.response;

import com.example.hrms.entity.Employee;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeResponse {
    private Integer employeeId;
    private String fullName;
    private String email;
    private String position;
    private Integer departmentId;
    private String departmentName;
    private LocalDate hireDate;
    private LocalDate dateOfBirth;
    private String gender;
    private String address;
    private String phone;
    private Employee.EmploymentStatus status;
    private Integer userId;
    private String username;
    private String roleKey;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String idCard;
    private String taxCode;
    private String bankAccount;
    private String bankName;
    private String emergencyContact;
    private String emergencyPhone;
    private String notes;
}


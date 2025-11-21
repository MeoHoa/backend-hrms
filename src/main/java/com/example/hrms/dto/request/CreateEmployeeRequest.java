package com.example.hrms.dto.request;

import com.example.hrms.entity.Employee;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateEmployeeRequest {
    @NotBlank(message = "Full name is required")
    private String fullName;

    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email")
    private String email;

    private String password;

    private Integer departmentId;

    private String position;
    private LocalDate hireDate;
    private LocalDate dateOfBirth;
    private String gender;
    private String address;
    private String phone;
    private Employee.EmploymentStatus status;
    private Integer roleId;
    private String idCard;
    private String taxCode;
    private String bankAccount;
    private String bankName;
    private String emergencyContact;
    private String emergencyPhone;
    private String notes;
}


package com.example.hrms.dto.request;

import com.example.hrms.entity.Employee;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateEmployeeRequest {
    private String fullName;
    private String username;
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
}



package com.example.hrms.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "employee")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer employeeId;

    private String position;

    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;

    private LocalDate hireDate;
    private LocalDate dateOfBirth;

    private String gender;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(unique = true)
    private String phone;

    @Enumerated(EnumType.STRING)
    private EmploymentStatus status;

    private java.time.LocalDateTime createdAt;
    private java.time.LocalDateTime updatedAt;
    @NotBlank
    @Column(name = "full_name")
    private String fullName;

    @Email
    @Column(unique = true)
    private String email;

    @Column(name = "id_card", length = 20)
    private String idCard;

    @Column(name = "tax_code", length = 20)
    private String taxCode;

    @Column(name = "bank_account", length = 30)
    private String bankAccount;

    @Column(name = "bank_name", length = 100)
    private String bankName;

    @Column(name = "emergency_contact", length = 150)
    private String emergencyContact;

    @Column(name = "emergency_phone", length = 15)
    private String emergencyPhone;

    private String notes;

    public enum Gender { Male, Female, Other }
    public enum EmploymentStatus { Active, Inactive, Resigned }
}

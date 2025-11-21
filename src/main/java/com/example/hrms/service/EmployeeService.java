package com.example.hrms.service;
import com.example.hrms.dto.request.*;
import com.example.hrms.dto.response.*;
import com.example.hrms.entity.Department;
import com.example.hrms.entity.Employee;
import com.example.hrms.entity.Role;
import com.example.hrms.entity.User;
import com.example.hrms.repository.DepartmentRepository;
import com.example.hrms.repository.EmployeeRepository;
import com.example.hrms.repository.RoleRepository;
import com.example.hrms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.security.SecureRandom;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Transactional
    public EmployeeResponse createEmployee(CreateEmployeeRequest request) {
        String email = request.getEmail();
        
        // Check if email already exists
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email already exists: " + email);
        }

        // Generate username from fullName using algorithm: tên.viết tắt họ, tên đệm
        String username = generateUsernameFromFullName(request.getFullName());
        
        // Check if username already exists
        if (userRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("Username already exists: " + username);
        }

        // Generate random password if not provided
        String password = request.getPassword();
        if (password == null || password.trim().isEmpty()) {
            password = generateRandomPassword();
        }

        // Check if phone already exists
        if (request.getPhone() != null && !request.getPhone().isEmpty()) {
            if (employeeRepository.findByPhone(request.getPhone()).isPresent()) {
                throw new RuntimeException("Phone number already exists: " + request.getPhone());
            }
        }

        // Get department
        Department department = null;
        if (request.getDepartmentId() != null) {
            department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new RuntimeException("Department not found with id: " + request.getDepartmentId()));
        }

        // Get role (default to EMPLOYEE if not provided)
        Role role = null;
        if (request.getRoleId() != null) {
            role = roleRepository.findById(request.getRoleId())
                    .orElseThrow(() -> new RuntimeException("Role not found with id: " + request.getRoleId()));
        } else {
            role = roleRepository.findByRoleKey("EMPLOYEE")
                    .orElseThrow(() -> new RuntimeException("Default EMPLOYEE role not found"));
        }

        // Create Employee (fullName is stored in Employee entity)
        Employee employee = Employee.builder()
                .fullName(request.getFullName())
                .email(email)
                .position(request.getPosition())
                .department(department)
                .hireDate(request.getHireDate())
                .dateOfBirth(request.getDateOfBirth())
                .gender(request.getGender())
                .address(request.getAddress())
                .phone(request.getPhone())
                .status(request.getStatus() != null ? request.getStatus() : Employee.EmploymentStatus.Active)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .idCard(request.getIdCard())
                .taxCode(request.getTaxCode())
                .bankAccount(request.getBankAccount())
                .bankName(request.getBankName())
                .emergencyContact(request.getEmergencyContact())
                .emergencyPhone(request.getEmergencyPhone())
                .notes(request.getNotes())
                .build();

        employee = employeeRepository.save(employee);

        // Create User (username is separate from email)
        // Set mustChangePassword = true for new users
        User user = User.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(password))
                .role(role)
                .employee(employee)
                .status(-1) // User must change password on first login
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        user = userRepository.save(user);

        // Send email with account information (username and password)
        try {
            emailService.sendAccountCreationEmail(
                email,
                request.getFullName(),
                username,
                password // Send plain password in email (this is the only time it's sent)
            );
            log.info("Account creation email sent to: {}", email);
        } catch (Exception e) {
            log.error("Failed to send account creation email to: {}", email, e);
            // Don't throw exception - account is already created, email failure shouldn't fail the operation
            // But log the error for admin to handle
        }

        return mapToResponse(employee, user);
    }

    public Page<EmployeeResponse> getAllEmployees(String search, Integer departmentId, Pageable pageable) {
        Page<Employee> employees = employeeRepository.searchEmployees(search, departmentId, pageable);
        return employees.map(employee -> {
            User user = userRepository.findByEmployee(employee)
                    .orElse(null);
            return mapToResponse(employee, user);
        });
    }

    public EmployeeResponse getEmployeeById(Integer id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found with id: " + id));

        User user = userRepository.findByEmployee(employee)
                .orElse(null);

        return mapToResponse(employee, user);
    }

    /**
     * Get employees of the department(s) managed by the current admin user
     * Admin can manage multiple departments, so this returns all employees from all managed departments
     */
    public Page<EmployeeResponse> getMyDepartmentEmployees(String search, Pageable pageable) {
        // Get current user from JWT token
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));

        // Get employee from current user
        if (currentUser.getEmployee() == null) {
            throw new RuntimeException("Current user does not have an associated employee");
        }
        Employee currentEmployee = currentUser.getEmployee();

        // Find all departments managed by this admin employee
        List<Department> managedDepartments = departmentRepository.findByAdmin(currentEmployee);

        if (managedDepartments.isEmpty()) {
            // Return empty page if admin doesn't manage any department
            return Page.empty(pageable);
        }

        // Get all employees from all managed departments
        Page<Employee> employees;
        if (search != null && !search.trim().isEmpty()) {
            // Search in all managed departments
            employees = employeeRepository.findByDepartmentsAndSearch(managedDepartments, search, pageable);
        } else {
            // Get all employees from all managed departments
            employees = employeeRepository.findByDepartments(managedDepartments, pageable);
        }

        return employees.map(employee -> {
            User user = userRepository.findByEmployee(employee)
                    .orElse(null);
            return mapToResponse(employee, user);
        });
    }

    public EmployeeResponse getMyEmployee(User currentUser) {
        if (currentUser.getEmployee() == null) {
            throw new RuntimeException("Employee not found for current user");
        }

        Employee employee = currentUser.getEmployee();
        return mapToResponse(employee, currentUser);
    }

    public MyEmployeeProfileResponse getMyEmployeeProfile(User currentUser) {
        if (currentUser.getEmployee() == null) {
            throw new RuntimeException("Employee not found for current user");
        }

        Employee employee = currentUser.getEmployee();
        return mapToMyProfileResponse(employee, currentUser);
    }

    @Transactional
    public MyEmployeeProfileResponse updateMyEmployeeProfile(User currentUser, UpdateMyEmployeeProfileRequest request) {
        if (currentUser.getEmployee() == null) {
            throw new RuntimeException("Employee not found for current user");
        }

        Employee employee = currentUser.getEmployee();

        // Only allow updating specific fields
        if (request.getFullName() != null) {
            employee.setFullName(request.getFullName());
        }
        if (request.getDateOfBirth() != null) {
            employee.setDateOfBirth(request.getDateOfBirth());
        }
        if (request.getGender() != null) {
            // Validate gender value
            String genderUpper = request.getGender().trim();
            if (!genderUpper.equalsIgnoreCase("Male") && 
                !genderUpper.equalsIgnoreCase("Female") && 
                !genderUpper.equalsIgnoreCase("Other")) {
                throw new RuntimeException("Invalid gender value. Must be: Male, Female, or Other");
            }
            // Employee.gender is String, not enum
            employee.setGender(genderUpper);
        }
        if (request.getAddress() != null) {
            employee.setAddress(request.getAddress());
        }
        if (request.getPhone() != null) {
            // Check if phone already exists for another employee
            final Integer currentEmployeeId = employee.getEmployeeId();
            employeeRepository.findByPhone(request.getPhone()).ifPresent(existingEmployee -> {
                if (!existingEmployee.getEmployeeId().equals(currentEmployeeId)) {
                    throw new RuntimeException("Phone number already exists");
                }
            });
            employee.setPhone(request.getPhone());
        }
        if (request.getEmergencyContact() != null) {
            employee.setEmergencyContact(request.getEmergencyContact());
        }
        if (request.getEmergencyPhone() != null) {
            employee.setEmergencyPhone(request.getEmergencyPhone());
        }
        if (request.getNotes() != null) {
            employee.setNotes(request.getNotes());
        }

        employee.setUpdatedAt(LocalDateTime.now());
        employee = employeeRepository.save(employee);

        return mapToMyProfileResponse(employee, currentUser);
    }

    private MyEmployeeProfileResponse mapToMyProfileResponse(Employee employee, User user) {
        MyEmployeeProfileResponse.MyEmployeeProfileResponseBuilder builder = MyEmployeeProfileResponse.builder()
                .employeeId(employee.getEmployeeId())
                .email(employee.getEmail())
                .position(employee.getPosition())
                .hireDate(employee.getHireDate())
                .fullName(employee.getFullName())
                .dateOfBirth(employee.getDateOfBirth())
                .gender(employee.getGender())
                .address(employee.getAddress())
                .phone(employee.getPhone())
                .emergencyContact(employee.getEmergencyContact())
                .emergencyPhone(employee.getEmergencyPhone())
                .notes(employee.getNotes())
                .userId(user.getUserId())
                .username(user.getUsername())
                .roleKey(user.getRole() != null ? user.getRole().getRoleKey() : null);

        if (employee.getDepartment() != null) {
            builder.departmentId(employee.getDepartment().getDepartmentId())
                    .departmentName(employee.getDepartment().getDepartmentName());
        }

        return builder.build();
    }

    @Transactional
    public EmployeeResponse updateEmployee(Integer id, UpdateEmployeeRequest request) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found with id: " + id));

        User user = userRepository.findByEmployee(employee)
                .orElseThrow(() -> new RuntimeException("User not found for employee id: " + id));

        // Update Employee fields
        if (request.getDepartmentId() != null) {
            Department department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new RuntimeException("Department not found with id: " + request.getDepartmentId()));
            employee.setDepartment(department);
        }
        if (request.getPosition() != null) {
            employee.setPosition(request.getPosition());
        }
        if (request.getHireDate() != null) {
            employee.setHireDate(request.getHireDate());
        }
        if (request.getDateOfBirth() != null) {
            employee.setDateOfBirth(request.getDateOfBirth());
        }
        if (request.getGender() != null) {
            employee.setGender(request.getGender());
        }
        if (request.getAddress() != null) {
            employee.setAddress(request.getAddress());
        }
        if (request.getPhone() != null) {
            // Check if phone is unique
            if (!request.getPhone().equals(employee.getPhone())) {
                if (employeeRepository.findByPhone(request.getPhone()).isPresent()) {
                    throw new RuntimeException("Phone number already exists: " + request.getPhone());
                }
            }
            employee.setPhone(request.getPhone());
        }
        if (request.getStatus() != null) {
            employee.setStatus(request.getStatus());
        }
        employee.setUpdatedAt(LocalDateTime.now());

        employee = employeeRepository.save(employee);

        // Update Employee fullName
        if (request.getFullName() != null) {
            employee.setFullName(request.getFullName());
        }
        
        // Update User fields
        if (request.getUsername() != null && !request.getUsername().equals(user.getUsername())) {
            // Check if username already exists
            if (userRepository.findByUsername(request.getUsername()).isPresent()) {
                throw new RuntimeException("Username already exists: " + request.getUsername());
            }
            user.setUsername(request.getUsername());
        }
        
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            // Check if email already exists
            if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                throw new RuntimeException("Email already exists: " + request.getEmail());
            }
            user.setEmail(request.getEmail());
            employee.setEmail(request.getEmail()); // Update email in Employee too
        }
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        if (request.getRoleId() != null) {
            Role role = roleRepository.findById(request.getRoleId())
                    .orElseThrow(() -> new RuntimeException("Role not found with id: " + request.getRoleId()));
            user.setRole(role);
        }
        user.setUpdatedAt(LocalDateTime.now());

        user = userRepository.save(user);

        return mapToResponse(employee, user);
    }

    @Transactional
    public void deleteEmployee(Integer id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found with id: " + id));

        // Soft delete by setting status to Inactive
        employee.setStatus(Employee.EmploymentStatus.Inactive);
        employee.setUpdatedAt(LocalDateTime.now());
        employeeRepository.save(employee);
    }

    private EmployeeResponse mapToResponse(Employee employee, User user) {
        EmployeeResponse.EmployeeResponseBuilder builder = EmployeeResponse.builder()
                .employeeId(employee.getEmployeeId())
                .position(employee.getPosition())
                .hireDate(employee.getHireDate())
                .dateOfBirth(employee.getDateOfBirth())
                .gender(employee.getGender())
                .address(employee.getAddress())
                .phone(employee.getPhone())
                .status(employee.getStatus())
                .createdAt(employee.getCreatedAt())
                .updatedAt(employee.getUpdatedAt())
                .idCard(employee.getIdCard())
                .taxCode(employee.getTaxCode())
                .bankAccount(employee.getBankAccount())
                .bankName(employee.getBankName())
                .emergencyContact(employee.getEmergencyContact())
                .emergencyPhone(employee.getEmergencyPhone())
                .notes(employee.getNotes());

        if (employee.getDepartment() != null) {
            builder.departmentId(employee.getDepartment().getDepartmentId())
                    .departmentName(employee.getDepartment().getDepartmentName());
        }

        // fullName and email are from Employee entity, not User
        builder.fullName(employee.getFullName())
                .email(employee.getEmail());
        
        if (user != null) {
            builder.userId(user.getUserId())
                    .username(user.getUsername());
            if (user.getRole() != null) {
                builder.roleKey(user.getRole().getRoleKey());
            }
        } else {
            builder.username(null); // Set username to null if user is not found
        }

        return builder.build();
    }

    /**
     * Generate username from full name using algorithm: tên.viết tắt họ, tên đệm
     * Example: "Hồ văn trung công" -> "cong.hvt"
     * If exists, append number: "cong.hvt1", "cong.hvt2", etc.
     */
    private String generateUsernameFromFullName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new RuntimeException("Full name cannot be empty for username generation");
        }

        // B1: Chuẩn hóa: bỏ khoảng trắng thừa, chuyển về lowercase
        String normalized = fullName.trim().toLowerCase();

        // B2: Chuyển về không dấu
        normalized = removeVietnameseAccent(normalized);

        // B3: Tách phần họ, tên đệm, tên
        String[] parts = normalized.split("\\s+");
        if (parts.length == 0) {
            throw new RuntimeException("Invalid full name format");
        }

        // B4: Lấy tên (phần cuối cùng)
        String ten = parts[parts.length - 1];

        // B5: Lấy ký tự đầu của họ và tên đệm
        StringBuilder initials = new StringBuilder();
        for (int i = 0; i < parts.length - 1; i++) {
            if (!parts[i].isEmpty()) {
                initials.append(parts[i].charAt(0));
            }
        }

        // B6: Ghép lại theo định dạng: ten.hoTenDemVietTat
        String baseUsername = ten + "." + initials;

        // B7: Kiểm tra trùng username (không phân biệt hoa/thường)
        int counter = 1;
        String username = baseUsername;

        while (userRepository.findByUsernameIgnoreCase(username).isPresent()) {
            username = baseUsername + counter;
            counter++;

            // tránh vòng lặp vô hạn nếu trùng quá nhiều
            if (counter > 1000) {
                username = baseUsername + System.currentTimeMillis();
                break;
            }
        }

        return username;
    }


    /**
     * Remove Vietnamese accents from a character
     */
    private String removeVietnameseAccent(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        // Convert Vietnamese characters to ASCII equivalents
        return text.replaceAll("[àáạảãâầấậẩẫăằắặẳẵ]", "a")
                .replaceAll("[èéẹẻẽêềếệểễ]", "e")
                .replaceAll("[ìíịỉĩ]", "i")
                .replaceAll("[òóọỏõôồốộổỗơờớợởỡ]", "o")
                .replaceAll("[ùúụủũưừứựửữ]", "u")
                .replaceAll("[ỳýỵỷỹ]", "y")
                .replaceAll("[đ]", "d")
                .replaceAll("[ÀÁẠẢÃÂẦẤẬẨẪĂẰẮẶẲẴ]", "A")
                .replaceAll("[ÈÉẸẺẼÊỀẾỆỂỄ]", "E")
                .replaceAll("[ÌÍỊỈĨ]", "I")
                .replaceAll("[ÒÓỌỎÕÔỒỐỘỔỖƠỜỚỢỞỠ]", "O")
                .replaceAll("[ÙÚỤỦŨƯỪỨỰỬỮ]", "U")
                .replaceAll("[ỲÝỴỶỸ]", "Y")
                .replaceAll("[Đ]", "D");
    }

    /**
     * Generate a random password
     */
    private String generateRandomPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();
        
        // Generate 12 character password
        for (int i = 0; i < 12; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        
        return password.toString();
    }
}


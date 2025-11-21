package com.example.hrms.config;

import com.example.hrms.entity.Department;
import com.example.hrms.entity.Employee;
import com.example.hrms.entity.HolidayCalendar;
import com.example.hrms.entity.LeaveType;
import com.example.hrms.entity.Role;
import com.example.hrms.entity.User;
import com.example.hrms.repository.DepartmentRepository;
import com.example.hrms.repository.EmployeeRepository;
import com.example.hrms.repository.HolidayCalendarRepository;
import com.example.hrms.repository.LeaveTypeRepository;
import com.example.hrms.repository.RoleRepository;
import com.example.hrms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final LeaveTypeRepository leaveTypeRepository;
    private final HolidayCalendarRepository holidayCalendarRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        log.info("Starting data initialization...");
        
        // Create or get default department
        Department defaultDepartment = departmentRepository.findAll().stream()
                .findFirst()
                .orElseGet(() -> {
                    Department dept = Department.builder()
                            .departmentName("General")
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build();
                    return departmentRepository.save(dept);
                });

        // Initialize roles
        Role adminRole = initializeRole("ADMIN", "Administrator");
        Role employeeRole = initializeRole("EMPLOYEE", "Employee");
        Role managerRole = initializeRole("MANAGER", "Manager");

        // Initialize leave types
        initializeLeaveTypes();

        // Initialize holidays
        initializeHolidays();

        // Initialize users and employees
        initializeUserAndEmployee("admin", "admin@hrms.com", "Admin User", adminRole, "Administrator", defaultDepartment, "0123456789");
        initializeUserAndEmployee("employee", "employee@hrms.com", "Employee User", employeeRole, "Staff", defaultDepartment, "0123456788");
        initializeUserAndEmployee("manager", "manager@hrms.com", "Manager User", managerRole, "Manager", defaultDepartment, "0123456787");

        log.info("Data initialization completed!");
    }

    private Role initializeRole(String roleKey, String roleName) {
        return roleRepository.findByRoleKey(roleKey)
                .orElseGet(() -> {
                    Role role = Role.builder()
                            .roleKey(roleKey)
                            .roleName(roleName)
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build();
                    Role saved = roleRepository.save(role);
                    log.info("Created role: {}", roleKey);
                    return saved;
                });
    }

    private void initializeLeaveTypes() {
        log.info("Initializing leave types...");
        
        // 1. Nghỉ phép năm (Annual Leave)
        initializeLeaveType(
            "Nghỉ phép năm",
            "Nghỉ phép có lương hàng năm, tính theo số tháng làm việc. Nhân viên được nghỉ 12 ngày/năm (pro-rated năm đầu)."
        );
        
        // 2. Nghỉ ốm (Sick Leave - BHXH)
        initializeLeaveType(
            "Nghỉ ốm (BHXH)",
            "Nghỉ phép do ốm đau, có giấy tờ chứng minh, được thanh toán theo chế độ bảo hiểm xã hội."
        );
        
        // 3. Nghỉ không lương (Unpaid Leave)
        initializeLeaveType(
            "Nghỉ không lương",
            "Nghỉ phép không lương, áp dụng khi nhân viên cần nghỉ nhưng không còn ngày phép có lương."
        );
        
        // 4. Nghỉ phép đặc biệt (Special Leave)
        initializeLeaveType(
            "Nghỉ phép đặc biệt",
            "Nghỉ phép đặc biệt cho các trường hợp như kết hôn, tang chế, nghỉ thai sản, v.v."
        );
        
        // 5. Nghỉ làm thêm giờ (Compensatory Leave)
        initializeLeaveType(
            "Nghỉ bù (Làm bù)",
            "Nghỉ phép để bù cho các ngày đã làm thêm giờ hoặc làm việc vào ngày lễ, cuối tuần."
        );
        
        log.info("Leave types initialization completed!");
    }

    private void initializeLeaveType(String leaveName, String description) {
        leaveTypeRepository.findByLeaveNameIgnoreCase(leaveName)
                .ifPresentOrElse(
                    existing -> log.info("Leave type '{}' already exists, skipping...", leaveName),
                    () -> {
                        LeaveType leaveType = LeaveType.builder()
                                .leaveName(leaveName)
                                .description(description)
                                .status(LeaveType.Status.Active)
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .build();
                        leaveTypeRepository.save(leaveType);
                        log.info("Created leave type: {}", leaveName);
                    }
                );
    }

    private void initializeUserAndEmployee(
            String username,
            String email,
            String fullName,
            Role role,
            String position,
            Department department,
            String phone) {
        
        // Check if user already exists (by username or email)
        if (userRepository.findByUsernameOrEmail(username).isPresent() || 
            userRepository.findByEmail(email).isPresent()) {
            log.info("User {} or {} already exists, skipping...", username, email);
            return;
        }

        // Check if email already exists in Employee, generate unique email if needed
        String uniqueEmail = email;
        int emailCounter = 1;
        while (true) {
            final String emailToCheck = uniqueEmail;
            boolean emailExists = employeeRepository.findAll().stream()
                    .anyMatch(e -> e.getEmail() != null && e.getEmail().equals(emailToCheck));
            if (!emailExists) {
                break;
            }
            String[] emailParts = email.split("@");
            uniqueEmail = emailParts[0] + emailCounter + "@" + (emailParts.length > 1 ? emailParts[1] : "hrms.com");
            emailCounter++;
            // Prevent infinite loop
            if (emailCounter > 100) {
                uniqueEmail = emailParts[0] + System.currentTimeMillis() + "@" + (emailParts.length > 1 ? emailParts[1] : "hrms.com");
                break;
            }
        }
        
        // Check if phone already exists, generate unique phone if needed
        String uniquePhone = phone;
        int counter = 1;
        while (employeeRepository.findByPhone(uniquePhone).isPresent()) {
            uniquePhone = phone.substring(0, Math.max(0, phone.length() - 1)) + counter;
            counter++;
        }

        // Generate unique values for additional fields based on timestamp and counter
        long timestamp = System.currentTimeMillis();
        String idCard = "CMND" + String.format("%09d", (timestamp % 1000000000)); // 9 digits
        String taxCode = "TAX" + String.format("%08d", (timestamp % 100000000)); // 8 digits
        String bankAccount = "ACC" + String.format("%09d", (timestamp % 1000000000)); // 9 digits
        
        // Create Employee with all required fields
        Employee employee = Employee.builder()
                .fullName(fullName)
                .email(uniqueEmail)
                .position(position)
                .department(department)
                .hireDate(LocalDate.now())
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .gender("Male")
                .address("123 Main Street, City, Country")
                .phone(uniquePhone)
                .idCard(idCard)
                .taxCode(taxCode)
                .bankAccount(bankAccount)
                .bankName("Vietcombank")
                .emergencyContact("Emergency Contact Person")
                .emergencyPhone("0987654321")
                .notes("Initial employee data created by DataInitializer")
                .status(Employee.EmploymentStatus.Active)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        employee = employeeRepository.save(employee);
        log.info("Created employee: {} with phone: {}, email: {}", fullName, uniquePhone, uniqueEmail);

        // Create User (use uniqueEmail to match Employee email)
        User user = User.builder()
                .username(username)
                .email(uniqueEmail)
                .password(passwordEncoder.encode("123456")) // Default password: 123456
                .role(role)
                .employee(employee)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        userRepository.save(user);
        log.info("Created user: {} (username: {}) with role: {} (password: 123456)", uniqueEmail, username, role.getRoleKey());
    }

    private void initializeHolidays() {
        log.info("Initializing holidays...");
        
        // Lấy năm hiện tại và năm sau
        int currentYear = LocalDate.now().getYear();
        int nextYear = currentYear + 1;
        
        // ============================================
        // Các ngày nghỉ lễ cố định của Việt Nam
        // Năm hiện tại (currentYear)
        // ============================================
        
        // Tết Dương lịch
        initializeHoliday("Tết Dương lịch", LocalDate.of(currentYear, 1, 1));
        
        // Tết Nguyên Đán 2025: 25/1 - 2/2 (9 ngày nghỉ)
        initializeHoliday("Tết Nguyên Đán - Mùng 1", LocalDate.of(currentYear, 1, 25));
        initializeHoliday("Tết Nguyên Đán - Mùng 2", LocalDate.of(currentYear, 1, 26));
        initializeHoliday("Tết Nguyên Đán - Mùng 3", LocalDate.of(currentYear, 1, 27));
        initializeHoliday("Tết Nguyên Đán - Mùng 4", LocalDate.of(currentYear, 1, 28));
        initializeHoliday("Tết Nguyên Đán - Mùng 5", LocalDate.of(currentYear, 1, 29));
        initializeHoliday("Tết Nguyên Đán - Mùng 6", LocalDate.of(currentYear, 1, 30));
        initializeHoliday("Tết Nguyên Đán - Mùng 7", LocalDate.of(currentYear, 1, 31));
        initializeHoliday("Tết Nguyên Đán - Mùng 8", LocalDate.of(currentYear, 2, 1));
        initializeHoliday("Tết Nguyên Đán - Mùng 9", LocalDate.of(currentYear, 2, 2));
        
        // Giỗ Tổ Hùng Vương (10/3 âm lịch) - 2025: 7/4
        initializeHoliday("Giỗ Tổ Hùng Vương", LocalDate.of(currentYear, 4, 7));
        
        // Giải phóng miền Nam
        initializeHoliday("Giải phóng miền Nam (30/4)", LocalDate.of(currentYear, 4, 30));
        
        // Quốc tế Lao động
        initializeHoliday("Quốc tế Lao động (1/5)", LocalDate.of(currentYear, 5, 1));
        
        // Quốc khánh (nghỉ 2 ngày: 2/9 và 3/9)
        initializeHoliday("Quốc khánh (2/9)", LocalDate.of(currentYear, 9, 2));
        initializeHoliday("Quốc khánh (3/9)", LocalDate.of(currentYear, 9, 3));
        
        // ============================================
        // Năm sau (nextYear)
        // ============================================
        
        // Tết Dương lịch
        initializeHoliday("Tết Dương lịch", LocalDate.of(nextYear, 1, 1));
        
        // Tết Nguyên Đán 2026: 17/1 - 25/1 (9 ngày nghỉ)
        initializeHoliday("Tết Nguyên Đán - Mùng 1", LocalDate.of(nextYear, 1, 17));
        initializeHoliday("Tết Nguyên Đán - Mùng 2", LocalDate.of(nextYear, 1, 18));
        initializeHoliday("Tết Nguyên Đán - Mùng 3", LocalDate.of(nextYear, 1, 19));
        initializeHoliday("Tết Nguyên Đán - Mùng 4", LocalDate.of(nextYear, 1, 20));
        initializeHoliday("Tết Nguyên Đán - Mùng 5", LocalDate.of(nextYear, 1, 21));
        initializeHoliday("Tết Nguyên Đán - Mùng 6", LocalDate.of(nextYear, 1, 22));
        initializeHoliday("Tết Nguyên Đán - Mùng 7", LocalDate.of(nextYear, 1, 23));
        initializeHoliday("Tết Nguyên Đán - Mùng 8", LocalDate.of(nextYear, 1, 24));
        initializeHoliday("Tết Nguyên Đán - Mùng 9", LocalDate.of(nextYear, 1, 25));
        
        // Giỗ Tổ Hùng Vương (10/3 âm lịch) - 2026: 26/3
        initializeHoliday("Giỗ Tổ Hùng Vương", LocalDate.of(nextYear, 3, 26));
        
        // Giải phóng miền Nam
        initializeHoliday("Giải phóng miền Nam (30/4)", LocalDate.of(nextYear, 4, 30));
        
        // Quốc tế Lao động
        initializeHoliday("Quốc tế Lao động (1/5)", LocalDate.of(nextYear, 5, 1));
        
        // Quốc khánh (nghỉ 2 ngày: 2/9 và 3/9)
        initializeHoliday("Quốc khánh (2/9)", LocalDate.of(nextYear, 9, 2));
        initializeHoliday("Quốc khánh (3/9)", LocalDate.of(nextYear, 9, 3));
        
        log.info("Holidays initialization completed!");
    }

    private void initializeHoliday(String holidayName, LocalDate holidayDate) {
        holidayCalendarRepository.findByHolidayDate(holidayDate)
                .ifPresentOrElse(
                    existing -> log.debug("Holiday '{}' on {} already exists, skipping...", holidayName, holidayDate),
                    () -> {
                        HolidayCalendar holiday = HolidayCalendar.builder()
                                .holidayName(holidayName)
                                .holidayDate(holidayDate)
                                .createdAt(LocalDateTime.now())
                                .build();
                        holidayCalendarRepository.save(holiday);
                        log.info("Created holiday: {} - {}", holidayName, holidayDate);
                    }
                );
    }
}


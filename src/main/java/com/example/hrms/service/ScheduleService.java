package com.example.hrms.service;

import com.example.hrms.entity.Department;
import com.example.hrms.entity.Employee;
import com.example.hrms.entity.User;
import com.example.hrms.entity.WorkSchedule;
import com.example.hrms.entity.WorkScheduleDepartment;
import com.example.hrms.entity.WorkScheduleEmployee;
import com.example.hrms.repository.DepartmentRepository;
import com.example.hrms.repository.EmployeeRepository;
import com.example.hrms.repository.UserRepository;
import com.example.hrms.repository.WorkScheduleRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import com.example.hrms.dto.request.*;
import com.example.hrms.dto.response.*;
@Service
@RequiredArgsConstructor
public class ScheduleService {
    private final WorkScheduleRepository workScheduleRepository;
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final EntityManager entityManager;

    private Employee getCurrentEmployee() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
        
        if (user.getEmployee() == null) {
            throw new RuntimeException("Current user does not have an associated employee");
        }
        return user.getEmployee();
    }

    public List<ScheduleResponse> getMySchedule(Integer month, Integer year) {
        Employee employee = getCurrentEmployee();

        // Default to current month/year if not provided
        LocalDate now = LocalDate.now();
        int targetMonth = month != null ? month : now.getMonthValue();
        int targetYear = year != null ? year : now.getYear();

        List<WorkSchedule> schedules = workScheduleRepository.findByEmployeeAndMonth(employee, targetYear, targetMonth);
        return schedules.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    public List<ScheduleResponse> getMyShifts(String range) {
        Employee employee = getCurrentEmployee();
        LocalDate today = LocalDate.now();
        String normalizedRange = range == null ? "week" : range.trim().toLowerCase();
        LocalDate startDate = today;
        LocalDate endDate;
        switch (normalizedRange) {
            case "day" -> endDate = today;
            case "month" -> endDate = today.plusDays(30);
            case "quarter" -> endDate = today.plusDays(90);
            default -> endDate = today.plusDays(7);
        }
        
        List<WorkSchedule> schedules = workScheduleRepository.findByEmployeeAndDateRange(employee, startDate, endDate);
        return schedules.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ScheduleResponse createSchedule(CreateScheduleRequest request) {
        // Validate time
        if (request.getToHour().isBefore(request.getFromHour()) || request.getToHour().equals(request.getFromHour())) {
            throw new RuntimeException("To hour must be after from hour");
        }

        // Get host department
        Department hostDepartment = departmentRepository.findById(request.getHostDepartmentId())
                .orElseThrow(() -> new RuntimeException("Host department not found with id: " + request.getHostDepartmentId()));

        // Get host employee
        Employee hostEmployee = employeeRepository.findById(request.getHostEmployeeId())
                .orElseThrow(() -> new RuntimeException("Host employee not found with id: " + request.getHostEmployeeId()));

        // Parse status (default: Active)
        WorkSchedule.Status status = WorkSchedule.Status.Active;
        if (request.getStatus() != null && !request.getStatus().trim().isEmpty()) {
            try {
                status = WorkSchedule.Status.valueOf(request.getStatus());
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid status: " + request.getStatus() + ". Valid values: Active, Cancelled, Postponed, Completed");
            }
        }

        // Create WorkSchedule
        WorkSchedule schedule = WorkSchedule.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .workDate(request.getDate())
                .fromHour(request.getFromHour())
                .toHour(request.getToHour())
                .meetingRoom(request.getMeetingRoom())
                .status(status)
                .hostDepartment(hostDepartment)
                .hostEmployee(hostEmployee)
                .departments(new ArrayList<>())
                .employees(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        schedule = workScheduleRepository.save(schedule);

        // Add participating departments
        if (request.getDepartmentIds() != null && !request.getDepartmentIds().isEmpty()) {
            for (Integer deptId : request.getDepartmentIds()) {
                Department department = departmentRepository.findById(deptId)
                        .orElseThrow(() -> new RuntimeException("Department not found with id: " + deptId));
                
                WorkScheduleDepartment wsDept = WorkScheduleDepartment.builder()
                        .workSchedule(schedule)
                        .department(department)
                        .build();
                schedule.getDepartments().add(wsDept);
            }
        }

        // Add participating employees
        if (request.getEmployeeIds() != null && !request.getEmployeeIds().isEmpty()) {
            for (Integer empId : request.getEmployeeIds()) {
                Employee employee = employeeRepository.findById(empId)
                        .orElseThrow(() -> new RuntimeException("Employee not found with id: " + empId));
                
                WorkScheduleEmployee wsEmp = WorkScheduleEmployee.builder()
                        .workSchedule(schedule)
                        .employee(employee)
                        .build();
                schedule.getEmployees().add(wsEmp);
            }
        }

        schedule = workScheduleRepository.save(schedule);
        return mapToResponse(schedule);
    }

    public List<ScheduleResponse> getAllEmployeesSchedules(Integer departmentId, LocalDate date) {
        List<WorkSchedule> schedules = workScheduleRepository.findByDepartmentAndDate(departmentId, date);
        return schedules.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ScheduleResponse updateSchedule(Integer workId, UpdateScheduleRequest request) {
        // Find existing schedule
        WorkSchedule schedule = workScheduleRepository.findById(workId)
                .orElseThrow(() -> new RuntimeException("Schedule not found with id: " + workId));

        // Update basic fields if provided
        if (request.getDate() != null) {
            schedule.setWorkDate(request.getDate());
        }
        
        if (request.getTitle() != null && !request.getTitle().trim().isEmpty()) {
            schedule.setTitle(request.getTitle());
        }
        
        if (request.getDescription() != null) {
            schedule.setDescription(request.getDescription());
        }
        
        LocalTime fromHour = request.getFromHour() != null ? request.getFromHour() : schedule.getFromHour();
        LocalTime toHour = request.getToHour() != null ? request.getToHour() : schedule.getToHour();
        
        // Validate time if either hour is updated
        if (request.getFromHour() != null || request.getToHour() != null) {
            if (toHour.isBefore(fromHour) || toHour.equals(fromHour)) {
                throw new RuntimeException("To hour must be after from hour");
            }
            schedule.setFromHour(fromHour);
            schedule.setToHour(toHour);
        }
        
        if (request.getMeetingRoom() != null) {
            schedule.setMeetingRoom(request.getMeetingRoom());
        }

        // Update status if provided
        if (request.getStatus() != null && !request.getStatus().trim().isEmpty()) {
            try {
                WorkSchedule.Status status = WorkSchedule.Status.valueOf(request.getStatus());
                schedule.setStatus(status);
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid status: " + request.getStatus() + ". Valid values: Active, Cancelled, Postponed, Completed");
            }
        }

        // Update host department if provided
        if (request.getHostDepartmentId() != null) {
            Department hostDepartment = departmentRepository.findById(request.getHostDepartmentId())
                    .orElseThrow(() -> new RuntimeException("Host department not found with id: " + request.getHostDepartmentId()));
            schedule.setHostDepartment(hostDepartment);
        }

        // Update host employee if provided
        if (request.getHostEmployeeId() != null) {
            Employee hostEmployee = employeeRepository.findById(request.getHostEmployeeId())
                    .orElseThrow(() -> new RuntimeException("Host employee not found with id: " + request.getHostEmployeeId()));
            schedule.setHostEmployee(hostEmployee);
        }

        // Update participating departments if provided
        // If departmentIds is provided (even if empty), replace all existing departments
        if (request.getDepartmentIds() != null) {
            // Clear and save to flush delete operations (orphanRemoval will handle deletion)
            schedule.getDepartments().clear();
            workScheduleRepository.saveAndFlush(schedule);
            
            // Reload to get fresh state
            entityManager.refresh(schedule);
            
            // Add new departments
            if (!request.getDepartmentIds().isEmpty()) {
                for (Integer deptId : request.getDepartmentIds()) {
                    Department department = departmentRepository.findById(deptId)
                            .orElseThrow(() -> new RuntimeException("Department not found with id: " + deptId));
                    
                    WorkScheduleDepartment wsDept = WorkScheduleDepartment.builder()
                            .workSchedule(schedule)
                            .department(department)
                            .build();
                    schedule.getDepartments().add(wsDept);
                }
            }
        }

        // Update participating employees if provided
        // If employeeIds is provided (even if empty), replace all existing employees
        if (request.getEmployeeIds() != null) {
            // Clear and save to flush delete operations (orphanRemoval will handle deletion)
            schedule.getEmployees().clear();
            workScheduleRepository.saveAndFlush(schedule);
            
            // Reload to get fresh state
            entityManager.refresh(schedule);
            
            // Add new employees
            if (!request.getEmployeeIds().isEmpty()) {
                for (Integer empId : request.getEmployeeIds()) {
                    Employee employee = employeeRepository.findById(empId)
                            .orElseThrow(() -> new RuntimeException("Employee not found with id: " + empId));
                    
                    WorkScheduleEmployee wsEmp = WorkScheduleEmployee.builder()
                            .workSchedule(schedule)
                            .employee(employee)
                            .build();
                    schedule.getEmployees().add(wsEmp);
                }
            }
        }

        schedule.setUpdatedAt(LocalDateTime.now());
        schedule = workScheduleRepository.save(schedule);
        
        return mapToResponse(schedule);
    }

    private ScheduleResponse mapToResponse(WorkSchedule schedule) {
        ScheduleResponse.ScheduleResponseBuilder builder = ScheduleResponse.builder()
                .workId(schedule.getWorkId())
                .title(schedule.getTitle())
                .description(schedule.getDescription())
                .workDate(schedule.getWorkDate())
                .fromHour(schedule.getFromHour())
                .toHour(schedule.getToHour())
                .meetingRoom(schedule.getMeetingRoom())
                .status(schedule.getStatus() != null ? schedule.getStatus().name() : WorkSchedule.Status.Active.name())
                .createdAt(schedule.getCreatedAt())
                .updatedAt(schedule.getUpdatedAt());

        // Host department info
        if (schedule.getHostDepartment() != null) {
            builder.hostDepartmentId(schedule.getHostDepartment().getDepartmentId())
                    .hostDepartmentName(schedule.getHostDepartment().getDepartmentName());
        }

        // Host employee info
        if (schedule.getHostEmployee() != null) {
            builder.hostEmployeeId(schedule.getHostEmployee().getEmployeeId())
                    .hostEmployeeFullName(schedule.getHostEmployee().getFullName())
                    .hostEmployeeEmail(schedule.getHostEmployee().getEmail());
        }

        // Participating departments
        if (schedule.getDepartments() != null && !schedule.getDepartments().isEmpty()) {
            List<ScheduleResponse.DepartmentInfo> deptInfos = schedule.getDepartments().stream()
                    .map(wsDept -> ScheduleResponse.DepartmentInfo.builder()
                            .departmentId(wsDept.getDepartment().getDepartmentId())
                            .departmentName(wsDept.getDepartment().getDepartmentName())
                            .build())
                    .collect(Collectors.toList());
            builder.departments(deptInfos);
        } else {
            builder.departments(new ArrayList<>());
        }

        // Participating employees
        if (schedule.getEmployees() != null && !schedule.getEmployees().isEmpty()) {
            List<ScheduleResponse.EmployeeInfo> empInfos = schedule.getEmployees().stream()
                    .map(wsEmp -> ScheduleResponse.EmployeeInfo.builder()
                            .employeeId(wsEmp.getEmployee().getEmployeeId())
                            .employeeFullName(wsEmp.getEmployee().getFullName())
                            .employeeEmail(wsEmp.getEmployee().getEmail())
                            .build())
                    .collect(Collectors.toList());
            builder.employees(empInfos);
        } else {
            builder.employees(new ArrayList<>());
        }

        return builder.build();
    }
}


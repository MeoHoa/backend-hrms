package com.example.hrms.service;

import com.example.hrms.dto.request.*;
import com.example.hrms.dto.response.*;
import com.example.hrms.entity.Department;
import com.example.hrms.entity.Employee;
import com.example.hrms.entity.LeaveType;
import com.example.hrms.repository.DepartmentRepository;
import com.example.hrms.repository.LeaveTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final DepartmentRepository departmentRepository;
    private final LeaveTypeRepository leaveTypeRepository;

    public List<DepartmentResponse> getAllDepartments() {
        return departmentRepository.findAll().stream()
                .map(this::mapToDepartmentResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public DepartmentResponse createDepartment(CreateDepartmentRequest request) {
        // Check if department name already exists
        boolean exists = departmentRepository.findAll().stream()
                .anyMatch(d -> d.getDepartmentName().equalsIgnoreCase(request.getDepartmentName()));

        if (exists) {
            throw new RuntimeException("Department with name already exists: " + request.getDepartmentName());
        }

        Department department = Department.builder()
                .departmentName(request.getDepartmentName())
                .description(request.getDescription())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        department = departmentRepository.save(department);
        return mapToDepartmentResponse(department);
    }

    public List<LeaveTypeResponse> getAllLeaveTypes() {
        return leaveTypeRepository.findAll().stream()
                .map(this::mapToLeaveTypeResponse)
                .collect(Collectors.toList());
    }

    public List<LeaveTypeSimpleResponse> getActiveLeaveTypes() {
        return leaveTypeRepository.findAll().stream()
                .filter(lt -> lt.getStatus() == LeaveType.Status.Active)
                .map(lt -> LeaveTypeSimpleResponse.builder()
                        .id(lt.getLeavetypeId())
                        .name(lt.getLeaveName())
                        .build())
                .collect(Collectors.toList());
    }

    private DepartmentResponse mapToDepartmentResponse(Department department) {
        DepartmentResponse.DepartmentResponseBuilder builder = DepartmentResponse.builder()
                .departmentId(department.getDepartmentId())
                .departmentName(department.getDepartmentName())
                .description(department.getDescription())
                .createdAt(department.getCreatedAt())
                .updatedAt(department.getUpdatedAt());

        if (department.getAdmin() != null) {
            Employee adminEmployee = department.getAdmin();
            builder.adminId(adminEmployee.getEmployeeId())
                    .adminName(adminEmployee.getFullName());
        }

        return builder.build();
    }

    private LeaveTypeResponse mapToLeaveTypeResponse(LeaveType leaveType) {
        return LeaveTypeResponse.builder()
                .leavetypeId(leaveType.getLeavetypeId())
                .leaveName(leaveType.getLeaveName())
                .description(leaveType.getDescription())
                .status(leaveType.getStatus())
                .createdAt(leaveType.getCreatedAt())
                .updatedAt(leaveType.getUpdatedAt())
                .build();
    }
}


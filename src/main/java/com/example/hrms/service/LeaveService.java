package com.example.hrms.service;
import com.example.hrms.dto.request.*;
import com.example.hrms.dto.response.*;
import com.example.hrms.entity.Employee;
import com.example.hrms.entity.LeaveType;
import com.example.hrms.entity.OnLeave;
import com.example.hrms.entity.User;
import com.example.hrms.repository.LeaveTypeRepository;
import com.example.hrms.repository.OnLeaveRepository;
import com.example.hrms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LeaveService {
    private final OnLeaveRepository onLeaveRepository;
    private final UserRepository userRepository;
    private final LeaveTypeRepository leaveTypeRepository;

    public User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
    }

    public Employee getCurrentEmployee() {
        User user = getCurrentUser();
        if (user.getEmployee() == null) {
            throw new RuntimeException("Current user does not have an associated employee");
        }
        return user.getEmployee();
    }

    @Transactional
    public LeaveRequestResponse createLeaveRequest(CreateLeaveRequest request) {
        Employee employee = getCurrentEmployee();

        // Validate dates
        if (request.getFromDate().isAfter(request.getToDate())) {
            throw new RuntimeException("From date cannot be after to date");
        }

        if (request.getFromDate().isBefore(LocalDate.now())) {
            throw new RuntimeException("From date cannot be in the past");
        }

        // Find leave type by ID
        LeaveType leaveType = leaveTypeRepository.findById(request.getLeavetypeId())
                .orElseThrow(() -> new RuntimeException("Leave type not found with id: " + request.getLeavetypeId()));

        // Check if leave type is active
        if (leaveType.getStatus() != LeaveType.Status.Active) {
            throw new RuntimeException("Leave type is not active: " + leaveType.getLeaveName());
        }

        // Calculate number of leave days (inclusive of both start and end date)
        long leaveDays = ChronoUnit.DAYS.between(request.getFromDate(), request.getToDate()) + 1;

        // Business logic: Check remaining annual leave days if it's annual leave
        if (leaveType.getLeaveName() != null && 
            (leaveType.getLeaveName().contains("Nghỉ phép năm") || 
             leaveType.getLeaveName().contains("Annual") ||
             leaveType.getLeaveName().toLowerCase().contains("annual leave"))) {
            
            int remainingDays = calculateRemainingAnnualLeaveDays(employee, LocalDate.now().getYear());
            if (leaveDays > remainingDays) {
                throw new RuntimeException(String.format(
                    "Insufficient annual leave days. Requested: %d days, Remaining: %d days", 
                    leaveDays, remainingDays));
            }
        }

        // Check for overlapping leave requests (same employee, overlapping dates, not rejected)
        List<OnLeave> existingLeaves = onLeaveRepository.findByEmployee(employee).stream()
                .filter(l -> l.getStatus() != OnLeave.Status.Rejected)
                .filter(l -> !(l.getToDate().isBefore(request.getFromDate()) || 
                              l.getFromDate().isAfter(request.getToDate())))
                .collect(Collectors.toList());
        
        if (!existingLeaves.isEmpty()) {
            throw new RuntimeException("You already have a leave request for this period");
        }

        // Get department admin: Find admin employee of the department that the employee belongs to
        // Then get the User account of that admin employee (if exists) for approval tracking
        User departmentAdminUser = null;
        if (employee.getDepartment() != null && employee.getDepartment().getAdmin() != null) {
            Employee departmentAdmin = employee.getDepartment().getAdmin();
            // Find User account associated with the admin employee
            departmentAdminUser = userRepository.findByEmployee(departmentAdmin).orElse(null);
        }

        // Create leave request with department admin user assigned (for approval tracking)
        OnLeave leaveRequest = OnLeave.builder()
                .employee(employee)
                .leaveType(leaveType)
                .reason(request.getReason())
                .fromDate(request.getFromDate())
                .toDate(request.getToDate())
                .status(OnLeave.Status.Pending)
                .admin(departmentAdminUser) // Assign department admin user (for approval tracking)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        leaveRequest = onLeaveRepository.save(leaveRequest);
        return mapToResponse(leaveRequest);
    }

    /**
     * Calculate remaining annual leave days for an employee in a given year
     * Logic: Based on hire date, calculate annual leave entitlement, 
     * then subtract already approved/annual leave days
     */
    private int calculateRemainingAnnualLeaveDays(Employee employee, int year) {
        if (employee.getHireDate() == null) {
            // Default to 12 days if employee info is not available
            return 12;
        }

        LocalDate hireDate = employee.getHireDate();
        LocalDate yearStart = LocalDate.of(year, 1, 1);
        LocalDate yearEnd = LocalDate.of(year, 12, 31);
        
        // Calculate annual leave entitlement based on hire date
        // Standard: 12 days per year, pro-rated based on months worked
        int annualLeaveEntitlement;
        
        if (hireDate.getYear() == year) {
            // First year: pro-rated based on months from hire date to end of year
            long monthsWorked = ChronoUnit.MONTHS.between(
                hireDate.withDayOfMonth(1), 
                yearEnd.withDayOfMonth(1).plusMonths(1)
            );
            annualLeaveEntitlement = (int) Math.round(12.0 * monthsWorked / 12.0);
        } else if (hireDate.getYear() < year) {
            // Full year entitlement
            annualLeaveEntitlement = 12;
        } else {
            // Future year (shouldn't happen)
            return 0;
        }
        
        // Calculate used annual leave days (only approved requests for this year)
        LocalDate startDate = yearStart.isBefore(hireDate) ? hireDate : yearStart;
        
        List<OnLeave> approvedAnnualLeaves = onLeaveRepository.findByEmployee(employee).stream()
                .filter(l -> l.getStatus() == OnLeave.Status.Approved)
                .filter(l -> l.getLeaveType() != null && 
                            (l.getLeaveType().getLeaveName().contains("Nghỉ phép năm") || 
                             l.getLeaveType().getLeaveName().contains("Annual") ||
                             l.getLeaveType().getLeaveName().toLowerCase().contains("annual leave")))
                .filter(l -> !l.getToDate().isBefore(startDate) && !l.getFromDate().isAfter(yearEnd))
                .collect(Collectors.toList());
        
        int usedDays = 0;
        for (OnLeave leave : approvedAnnualLeaves) {
            LocalDate actualStart = leave.getFromDate().isBefore(startDate) ? startDate : leave.getFromDate();
            LocalDate actualEnd = leave.getToDate().isAfter(yearEnd) ? yearEnd : leave.getToDate();
            usedDays += (int) (ChronoUnit.DAYS.between(actualStart, actualEnd) + 1);
        }
        
        return Math.max(0, annualLeaveEntitlement - usedDays);
    }

    public List<LeaveRequestResponse> getMyLeaveRequests(
            String status,
            Integer month,
            Integer year,
            Integer limit) {
        Employee employee = getCurrentEmployee();

        OnLeave.Status statusEnum = null;
        if (status != null && !status.trim().isEmpty()) {
            try {
                statusEnum = OnLeave.Status.valueOf(status.trim());
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid status: " + status);
            }
        }

        LocalDate startDate = null;
        LocalDate endDate = null;

        // If month/year is provided, calculate startDate and endDate from them
        if (month != null && year != null) {
            // Validate month (1-12) and year
            if (month < 1 || month > 12) {
                throw new RuntimeException("Month must be between 1 and 12");
            }
            if (year < 2000 || year > 2100) {
                throw new RuntimeException("Year must be between 2000 and 2100");
            }
            
            // Calculate first day and last day of the month
            startDate = LocalDate.of(year, month, 1);
            // Get last day of month
            endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
        }

        List<OnLeave> requests;
        if (startDate != null && endDate != null) {
            // Filter by date range (leave requests that overlap with the month)
            requests = onLeaveRepository.findByEmployeeAndStatusAndDateRange(employee, statusEnum, startDate, endDate);
        } else {
            // No date filter, get all
            requests = onLeaveRepository.findByEmployeeAndStatus(employee, statusEnum);
        }

        List<LeaveRequestResponse> responses = requests.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        
        if (limit != null && limit > 0) {
            return responses.stream()
                    .sorted((a, b) -> {
                        if (a.getCreatedAt() == null || b.getCreatedAt() == null) {
                            return 0;
                        }
                        return b.getCreatedAt().compareTo(a.getCreatedAt());
                    })
                    .limit(limit)
                    .collect(Collectors.toList());
        }
        
        return responses;
    }
    
    public LeaveSummaryResponse getMyLeaveSummary() {
        Employee employee = getCurrentEmployee();
        List<OnLeave> requests = onLeaveRepository.findByEmployee(employee);
        
        int totalDays = 0;
        int approvedDays = 0;
        int pendingDays = 0;
        int rejectedDays = 0;
        
        java.util.Map<String, LeaveSummaryResponse.LeaveTypeSummary> typeSummaryMap = new java.util.HashMap<>();
        
        for (OnLeave request : requests) {
            int days = calculateLeaveDays(request);
            totalDays += days;
            if (request.getStatus() == OnLeave.Status.Approved) {
                approvedDays += days;
            } else if (request.getStatus() == OnLeave.Status.Pending) {
                pendingDays += days;
            } else if (request.getStatus() == OnLeave.Status.Rejected) {
                rejectedDays += days;
            }
            
            String typeName = request.getLeaveType() != null ? request.getLeaveType().getLeaveName() : "Khác";
            LeaveSummaryResponse.LeaveTypeSummary summary = typeSummaryMap.computeIfAbsent(
                    typeName,
                    key -> LeaveSummaryResponse.LeaveTypeSummary.builder()
                            .leaveTypeName(key)
                            .totalDays(0)
                            .approvedDays(0)
                            .pendingDays(0)
                            .build());
            
            summary.setTotalDays(summary.getTotalDays() + days);
            if (request.getStatus() == OnLeave.Status.Approved) {
                summary.setApprovedDays(summary.getApprovedDays() + days);
            } else if (request.getStatus() == OnLeave.Status.Pending) {
                summary.setPendingDays(summary.getPendingDays() + days);
            }
        }
        
        int entitlement = 12; // Default annual leave entitlement
        int remaining = calculateRemainingAnnualLeaveDays(employee, LocalDate.now().getYear());
        int used = entitlement - remaining;
        
        return LeaveSummaryResponse.builder()
                .totalLeaveDays(totalDays)
                .approvedLeaveDays(approvedDays)
                .pendingLeaveDays(pendingDays)
                .rejectedLeaveDays(rejectedDays)
                .annualLeaveEntitlement(entitlement)
                .usedAnnualLeaveDays(Math.max(0, used))
                .remainingAnnualLeaveDays(Math.max(0, remaining))
                .leaveDaysByType(typeSummaryMap.entrySet().stream()
                        .collect(java.util.stream.Collectors.toMap(
                                java.util.Map.Entry::getKey,
                                entry -> entry.getValue().getTotalDays()
                        )))
                .leaveTypeDetails(new java.util.ArrayList<>(typeSummaryMap.values()))
                .build();
    }

    private int calculateLeaveDays(OnLeave request) {
        if (request.getFromDate() == null || request.getToDate() == null) {
            return 0;
        }
        return (int) (java.time.temporal.ChronoUnit.DAYS.between(request.getFromDate(), request.getToDate()) + 1);
    }

    @Transactional
    public LeaveRequestResponse updateLeaveRequest(Integer requestId, UpdateLeaveRequest request) {
        Employee employee = getCurrentEmployee();
        
        // Find the leave request
        OnLeave leaveRequest = onLeaveRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Leave request not found with id: " + requestId));

        // Check if the leave request belongs to the current employee
        if (leaveRequest.getEmployee() == null || !leaveRequest.getEmployee().getEmployeeId().equals(employee.getEmployeeId())) {
            throw new RuntimeException("You can only update your own leave requests");
        }

        // Check if the leave request can be updated (only Pending requests can be updated)
        if (leaveRequest.getStatus() != OnLeave.Status.Pending) {
            throw new RuntimeException("Only pending leave requests can be updated");
        }

        // Validate dates
        if (request.getFromDate().isAfter(request.getToDate())) {
            throw new RuntimeException("From date cannot be after to date");
        }

        if (request.getFromDate().isBefore(LocalDate.now())) {
            throw new RuntimeException("From date cannot be in the past");
        }

        // Find leave type by ID
        LeaveType leaveType = leaveTypeRepository.findById(request.getLeavetypeId())
                .orElseThrow(() -> new RuntimeException("Leave type not found with id: " + request.getLeavetypeId()));

        // Check if leave type is active
        if (leaveType.getStatus() != LeaveType.Status.Active) {
            throw new RuntimeException("Leave type is not active: " + leaveType.getLeaveName());
        }

        // Calculate number of leave days
        long leaveDays = ChronoUnit.DAYS.between(request.getFromDate(), request.getToDate()) + 1;

        // Business logic: Check remaining annual leave days if it's annual leave
        if (leaveType.getLeaveName() != null && 
            (leaveType.getLeaveName().contains("Nghỉ phép năm") || 
             leaveType.getLeaveName().contains("Annual") ||
             leaveType.getLeaveName().toLowerCase().contains("annual leave"))) {
            
            // Calculate remaining days excluding the current request
            int remainingDays = calculateRemainingAnnualLeaveDays(employee, LocalDate.now().getYear());
            
            // If the current request was annual leave, add those days back
            if (leaveRequest.getLeaveType() != null && 
                (leaveRequest.getLeaveType().getLeaveName().contains("Nghỉ phép năm") || 
                 leaveRequest.getLeaveType().getLeaveName().contains("Annual") ||
                 leaveRequest.getLeaveType().getLeaveName().toLowerCase().contains("annual leave"))) {
                long currentRequestDays = ChronoUnit.DAYS.between(
                    leaveRequest.getFromDate(), leaveRequest.getToDate()) + 1;
                remainingDays += (int) currentRequestDays;
            }
            
            if (leaveDays > remainingDays) {
                throw new RuntimeException(String.format(
                    "Insufficient annual leave days. Requested: %d days, Remaining: %d days", 
                    leaveDays, remainingDays));
            }
        }

        // Check for overlapping leave requests (exclude current request)
        List<OnLeave> existingLeaves = onLeaveRepository.findByEmployee(employee).stream()
                .filter(l -> !l.getRequestId().equals(requestId)) // Exclude current request
                .filter(l -> l.getStatus() != OnLeave.Status.Rejected)
                .filter(l -> !(l.getToDate().isBefore(request.getFromDate()) || 
                              l.getFromDate().isAfter(request.getToDate())))
                .collect(Collectors.toList());
        
        if (!existingLeaves.isEmpty()) {
            throw new RuntimeException("You already have a leave request for this period");
        }

        // Update leave request
        leaveRequest.setLeaveType(leaveType);
        leaveRequest.setFromDate(request.getFromDate());
        leaveRequest.setToDate(request.getToDate());
        leaveRequest.setReason(request.getReason());
        leaveRequest.setUpdatedAt(LocalDateTime.now());
        
        // Update department admin user if employee's department changed
        if (employee.getDepartment() != null && employee.getDepartment().getAdmin() != null) {
            Employee departmentAdmin = employee.getDepartment().getAdmin();
            // Find User account associated with the admin employee
            User departmentAdminUser = userRepository.findByEmployee(departmentAdmin).orElse(null);
            leaveRequest.setAdmin(departmentAdminUser);
        }

        leaveRequest = onLeaveRepository.save(leaveRequest);
        return mapToResponse(leaveRequest);
    }

    @Transactional
    public void deleteLeaveRequest(Integer requestId) {
        Employee employee = getCurrentEmployee();
        
        // Find the leave request
        OnLeave leaveRequest = onLeaveRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Leave request not found with id: " + requestId));

        // Check if the leave request belongs to the current employee
        if (leaveRequest.getEmployee() == null || !leaveRequest.getEmployee().getEmployeeId().equals(employee.getEmployeeId())) {
            throw new RuntimeException("You can only delete your own leave requests");
        }

        // Check if the leave request can be deleted (only Pending requests can be deleted)
        if (leaveRequest.getStatus() != OnLeave.Status.Pending) {
            throw new RuntimeException("Only pending leave requests can be deleted");
        }

        // Delete the leave request
        onLeaveRepository.delete(leaveRequest);
    }

    public Page<LeaveRequestResponse> getPendingLeaveRequests(Pageable pageable) {
        Page<OnLeave> pendingRequests = onLeaveRepository.findByStatus(OnLeave.Status.Pending, pageable);
        return pendingRequests.map(this::mapToResponse);
    }

    public Page<LeaveRequestResponse> getAllLeaveRequests(
            OnLeave.Status status,
            Integer employeeId,
            Integer leaveTypeId,
            Integer month,
            Integer year,
            Pageable pageable) {
        
        LocalDate startDate = null;
        LocalDate endDate = null;

        // If month/year is provided, calculate startDate and endDate from them
        if (month != null && year != null) {
            // Validate month (1-12) and year
            if (month < 1 || month > 12) {
                throw new RuntimeException("Month must be between 1 and 12");
            }
            if (year < 2000 || year > 2100) {
                throw new RuntimeException("Year must be between 2000 and 2100");
            }
            
            // Calculate first day and last day of the month
            startDate = LocalDate.of(year, month, 1);
            // Get last day of month
            endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
        }

        Page<OnLeave> requests = onLeaveRepository.findAllWithFilters(
                status, employeeId, leaveTypeId, startDate, endDate, pageable);
        return requests.map(this::mapToResponse);
    }

    @Transactional
    public LeaveRequestResponse approveLeaveRequest(Integer requestId) {
        User admin = getCurrentUser();
        
        OnLeave leaveRequest = onLeaveRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Leave request not found with id: " + requestId));

        if (leaveRequest.getStatus() != OnLeave.Status.Pending) {
            throw new RuntimeException("Only pending requests can be approved");
        }

        leaveRequest.setStatus(OnLeave.Status.Approved);
        leaveRequest.setAdmin(admin);
        leaveRequest.setProcessedDate(LocalDateTime.now());
        leaveRequest.setUpdatedAt(LocalDateTime.now());
        leaveRequest = onLeaveRepository.save(leaveRequest);

        return mapToResponse(leaveRequest);
    }

    @Transactional
    public LeaveRequestResponse rejectLeaveRequest(Integer requestId, RejectLeaveRequest request) {
        User admin = getCurrentUser();
        
        OnLeave leaveRequest = onLeaveRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Leave request not found with id: " + requestId));

        if (leaveRequest.getStatus() != OnLeave.Status.Pending) {
            throw new RuntimeException("Only pending requests can be rejected");
        }

        leaveRequest.setStatus(OnLeave.Status.Rejected);
        leaveRequest.setAdmin(admin);
        leaveRequest.setProcessedDate(LocalDateTime.now());
        
        // Store rejection reason appended to the reason field
        // Note: The entity doesn't have a separate rejectionReason field, so we append it
        if (request.getRejectionReason() != null && !request.getRejectionReason().trim().isEmpty()) {
            String originalReason = leaveRequest.getReason() != null ? leaveRequest.getReason() : "";
            leaveRequest.setReason(originalReason + " [REJECTION_REASON: " + request.getRejectionReason() + "]");
        }
        
        leaveRequest.setUpdatedAt(LocalDateTime.now());
        leaveRequest = onLeaveRepository.save(leaveRequest);

        return mapToResponse(leaveRequest, request.getRejectionReason());
    }

    private LeaveRequestResponse mapToResponse(OnLeave leaveRequest) {
        return mapToResponse(leaveRequest, null);
    }

    private LeaveRequestResponse mapToResponse(OnLeave leaveRequest, String rejectionReason) {
        LeaveRequestResponse.LeaveRequestResponseBuilder builder = LeaveRequestResponse.builder()
                .requestId(leaveRequest.getRequestId())
                .fromDate(leaveRequest.getFromDate())
                .toDate(leaveRequest.getToDate())
                .status(leaveRequest.getStatus())
                .processedDate(leaveRequest.getProcessedDate())
                .createdAt(leaveRequest.getCreatedAt())
                .updatedAt(leaveRequest.getUpdatedAt());

        if (leaveRequest.getEmployee() != null) {
            Employee emp = leaveRequest.getEmployee();
            builder.employeeId(emp.getEmployeeId())
                    .employeeFullName(emp.getFullName())
                    .employeeEmail(emp.getEmail());
            
            // Also include userId if employee has associated user
            if (emp.getEmployeeId() != null) {
                userRepository.findByEmployee(emp).ifPresent(user -> {
                    builder.userId(user.getUserId());
                });
            }
        }

        if (leaveRequest.getLeaveType() != null) {
            builder.leaveTypeId(leaveRequest.getLeaveType().getLeavetypeId())
                    .leaveType(leaveRequest.getLeaveType().getLeaveName());
        }

        if (leaveRequest.getAdmin() != null) {
            builder.adminId(leaveRequest.getAdmin().getUserId())
                    .adminFullName(leaveRequest.getAdmin().getEmployee() != null ? 
                                 leaveRequest.getAdmin().getEmployee().getFullName() : 
                                 leaveRequest.getAdmin().getUsername());
        }

        // Handle rejection reason - extract it from reason field if present
        String originalReason = leaveRequest.getReason();
        if (rejectionReason != null) {
            // If rejection reason is provided directly, use it and preserve original reason
            builder.rejectionReason(rejectionReason);
            // Keep original reason without the rejection part
            if (originalReason != null && originalReason.contains("[REJECTION_REASON:")) {
                originalReason = originalReason.substring(0, originalReason.indexOf("[REJECTION_REASON:")).trim();
            }
            builder.reason(originalReason);
        } else if (leaveRequest.getStatus() == OnLeave.Status.Rejected && originalReason != null) {
            // Try to extract rejection reason from the reason field
            if (originalReason.contains("[REJECTION_REASON:")) {
                int start = originalReason.indexOf("[REJECTION_REASON:") + "[REJECTION_REASON:".length();
                int end = originalReason.indexOf("]", start);
                if (end > start) {
                    String extractedReason = originalReason.substring(start, end).trim();
                    builder.rejectionReason(extractedReason);
                    // Clean the original reason field
                    originalReason = originalReason.substring(0, originalReason.indexOf("[REJECTION_REASON:")).trim();
                }
            }
            builder.reason(originalReason);
        } else {
            builder.reason(originalReason);
        }

        return builder.build();
    }
}
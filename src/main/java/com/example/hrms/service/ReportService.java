package com.example.hrms.service;

import com.example.hrms.entity.*;
import com.example.hrms.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;
import com.example.hrms.dto.request.*;
import com.example.hrms.dto.response.*;
@Service
@RequiredArgsConstructor
public class ReportService {
    private final TimekeepingRepository timekeepingRepository;
    private final OnLeaveRepository onLeaveRepository;
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final LeaveTypeRepository leaveTypeRepository;

    public List<TimekeepingSummaryResponse> getTimekeepingSummary(Integer month, Integer year, Integer employeeId) {
        LocalDate now = LocalDate.now();
        int targetMonth = month != null ? month : now.getMonthValue();
        int targetYear = year != null ? year : now.getYear();

        LocalDate startDate = LocalDate.of(targetYear, targetMonth, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        // Get employees to process
        List<Employee> employees;
        if (employeeId != null) {
            employees = employeeRepository.findById(employeeId)
                    .map(Collections::singletonList)
                    .orElse(Collections.emptyList());
        } else {
            employees = employeeRepository.findAll();
        }

        List<TimekeepingSummaryResponse> summaries = new ArrayList<>();

        for (Employee employee : employees) {
            List<com.example.hrms.entity.Timekeeping> timekeepingRecords = timekeepingRepository
                    .findAll().stream()
                    .filter(t -> t.getEmployee() != null && t.getEmployee().getEmployeeId().equals(employee.getEmployeeId()))
                    .filter(t -> !t.getWorkDate().isBefore(startDate) && !t.getWorkDate().isAfter(endDate))
                    .filter(t -> t.getStatus() == com.example.hrms.entity.Timekeeping.Status.Confirmed)
                    .collect(Collectors.toList());

            int totalWorkDays = timekeepingRecords.size();
            BigDecimal totalWorkHours = timekeepingRecords.stream()
                    .filter(t -> t.getWorkHours() != null)
                    .map(com.example.hrms.entity.Timekeeping::getWorkHours)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalOvertimeHours = timekeepingRecords.stream()
                    .filter(t -> t.getOvertimeHours() != null)
                    .map(com.example.hrms.entity.Timekeeping::getOvertimeHours)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Count late arrivals (check-in after 8:30 AM) and early departures (check-out before 5:30 PM)
            int lateArrivals = (int) timekeepingRecords.stream()
                    .filter(t -> t.getCheckIn() != null)
                    .filter(t -> t.getCheckIn().toLocalTime().isAfter(LocalTime.of(8, 30)))
                    .count();

            int earlyDepartures = (int) timekeepingRecords.stream()
                    .filter(t -> t.getCheckOut() != null)
                    .filter(t -> t.getCheckOut().toLocalTime().isBefore(LocalTime.of(17, 30)))
                    .count();

            // Calculate attendance rate (work days / total working days in month)
            int totalWorkingDaysInMonth = calculateWorkingDaysInMonth(targetYear, targetMonth);
            BigDecimal attendanceRate = totalWorkingDaysInMonth > 0
                    ? BigDecimal.valueOf(totalWorkDays)
                            .divide(BigDecimal.valueOf(totalWorkingDaysInMonth), 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100))
                    : BigDecimal.ZERO;

            summaries.add(TimekeepingSummaryResponse.builder()
                    .employeeId(employee.getEmployeeId())
                    .employeeName(employee.getFullName())
                    .employeeEmail(employee.getEmail())
                    .totalWorkDays(totalWorkDays)
                    .totalWorkHours(totalWorkHours)
                    .totalOvertimeHours(totalOvertimeHours)
                    .lateArrivals(lateArrivals)
                    .earlyDepartures(earlyDepartures)
                    .attendanceRate(attendanceRate)
                    .build());
        }

        return summaries;
    }

    public LeaveSummaryResponse getLeaveSummary(Integer month, Integer year) {
        LocalDate now = LocalDate.now();
        int targetMonth = month != null ? month : now.getMonthValue();
        int targetYear = year != null ? year : now.getYear();

        LocalDate startDate = LocalDate.of(targetYear, targetMonth, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        // Get all leave requests in the month
        List<OnLeave> leaveRequests = onLeaveRepository.findAll().stream()
                .filter(l -> {
                    // Check if leave period overlaps with the target month
                    return !l.getFromDate().isAfter(endDate) && !l.getToDate().isBefore(startDate);
                })
                .collect(Collectors.toList());

        int totalLeaveDays = 0;
        int approvedLeaveDays = 0;
        int pendingLeaveDays = 0;
        int rejectedLeaveDays = 0;

        Map<String, Integer> leaveDaysByType = new HashMap<>();
        Map<String, LeaveSummaryResponse.LeaveTypeSummary> leaveTypeDetailsMap = new HashMap<>();

        for (OnLeave leave : leaveRequests) {
            // Calculate actual days in the target month
            LocalDate actualStart = leave.getFromDate().isBefore(startDate) ? startDate : leave.getFromDate();
            LocalDate actualEnd = leave.getToDate().isAfter(endDate) ? endDate : leave.getToDate();

            long days = java.time.temporal.ChronoUnit.DAYS.between(actualStart, actualEnd) + 1;
            int daysInt = (int) days;

            totalLeaveDays += daysInt;

            String leaveTypeName = leave.getLeaveType() != null ? leave.getLeaveType().getLeaveName() : "Unknown";

            if (leave.getStatus() == OnLeave.Status.Approved) {
                approvedLeaveDays += daysInt;
            } else if (leave.getStatus() == OnLeave.Status.Pending) {
                pendingLeaveDays += daysInt;
            } else if (leave.getStatus() == OnLeave.Status.Rejected) {
                rejectedLeaveDays += daysInt;
            }

            // By type
            leaveDaysByType.merge(leaveTypeName, daysInt, Integer::sum);

            LeaveSummaryResponse.LeaveTypeSummary summary = leaveTypeDetailsMap.computeIfAbsent(
                    leaveTypeName,
                    k -> LeaveSummaryResponse.LeaveTypeSummary.builder()
                            .leaveTypeName(leaveTypeName)
                            .totalDays(0)
                            .approvedDays(0)
                            .pendingDays(0)
                            .build());

            summary.setTotalDays(summary.getTotalDays() + daysInt);
            if (leave.getStatus() == OnLeave.Status.Approved) {
                summary.setApprovedDays(summary.getApprovedDays() + daysInt);
            } else if (leave.getStatus() == OnLeave.Status.Pending) {
                summary.setPendingDays(summary.getPendingDays() + daysInt);
            }
        }

        return LeaveSummaryResponse.builder()
                .totalLeaveDays(totalLeaveDays)
                .approvedLeaveDays(approvedLeaveDays)
                .pendingLeaveDays(pendingLeaveDays)
                .rejectedLeaveDays(rejectedLeaveDays)
                .leaveDaysByType(leaveDaysByType)
                .leaveTypeDetails(new ArrayList<>(leaveTypeDetailsMap.values()))
                .build();
    }

    public DashboardStatsResponse getDashboardStats() {
        // Total employees
        long totalEmployees = employeeRepository.count();
        long activeEmployees = employeeRepository.findAll().stream()
                .filter(e -> e.getStatus() == Employee.EmploymentStatus.Active)
                .count();

        // Total departments
        long totalDepartments = departmentRepository.count();

        // Pending leave requests
        long pendingLeaveRequests = onLeaveRepository.findByStatus(OnLeave.Status.Pending).size();

        // Pending timekeeping records
        long pendingTimekeepingRecords = timekeepingRepository.findByStatus(Timekeeping.Status.Pending).size();

        // Calculate overall attendance rate (this month)
        LocalDate now = LocalDate.now();
        int currentMonth = now.getMonthValue();
        int currentYear = now.getYear();
        List<TimekeepingSummaryResponse> timekeepingSummary = getTimekeepingSummary(currentMonth, currentYear, null);
        
        BigDecimal overallAttendanceRate = BigDecimal.ZERO;
        if (!timekeepingSummary.isEmpty()) {
            BigDecimal sumRate = timekeepingSummary.stream()
                    .map(TimekeepingSummaryResponse::getAttendanceRate)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            overallAttendanceRate = sumRate.divide(BigDecimal.valueOf(timekeepingSummary.size()), 2, RoundingMode.HALF_UP);
        }

        // Today's check-ins and absences
        LocalDate today = LocalDate.now();
        long todayCheckIns = timekeepingRepository.findAll().stream()
                .filter(t -> t.getWorkDate().equals(today) && t.getCheckIn() != null)
                .count();

        long todayExpectedEmployees = employeeRepository.findAll().stream()
                .filter(e -> e.getStatus() == Employee.EmploymentStatus.Active)
                .count();
        long todayAbsences = todayExpectedEmployees - todayCheckIns;

        return DashboardStatsResponse.builder()
                .totalEmployees((int) totalEmployees)
                .activeEmployees((int) activeEmployees)
                .totalDepartments((int) totalDepartments)
                .pendingLeaveRequests((int) pendingLeaveRequests)
                .pendingTimekeepingRecords((int) pendingTimekeepingRecords)
                .overallAttendanceRate(overallAttendanceRate)
                .todayCheckIns((int) todayCheckIns)
                .todayAbsences((int) todayAbsences)
                .build();
    }

    private int calculateWorkingDaysInMonth(int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        int workingDays = 0;
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            // Exclude weekends (Saturday = 6, Sunday = 7)
            int dayOfWeek = current.getDayOfWeek().getValue();
            if (dayOfWeek != 6 && dayOfWeek != 7) {
                workingDays++;
            }
            current = current.plusDays(1);
        }
        return workingDays;
    }
}


package com.example.hrms.service;

import com.example.hrms.dto.response.*;
import com.example.hrms.entity.Employee;
import com.example.hrms.entity.OnLeave;
import com.example.hrms.entity.Timekeeping;
import com.example.hrms.entity.WorkSchedule;
import com.example.hrms.entity.WorkScheduleDepartment;
import com.example.hrms.entity.WorkScheduleEmployee;
import com.example.hrms.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService {
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final TimekeepingRepository timekeepingRepository;
    private final OnLeaveRepository onLeaveRepository;
    private final HolidayCalendarRepository holidayCalendarRepository;
    private final WorkScheduleRepository workScheduleRepository;
    private final TimekeepingService timekeepingService;

    public AdminDashboardSummaryResponse getAdminSummary() {
        LocalDate today = LocalDate.now();
        LocalDate firstDayOfMonth = today.withDayOfMonth(1);

        long totalEmployees = employeeRepository.count();
        long activeEmployees = employeeRepository.countByStatus(Employee.EmploymentStatus.Active);
        long pendingTimekeeping = timekeepingRepository.countByStatus(Timekeeping.Status.Pending);
        long pendingLeaveRequests = onLeaveRepository.countByStatus(OnLeave.Status.Pending);
        long todayCheckIns = timekeepingRepository.countByWorkDateAndCheckInIsNotNull(today);
        long todayAbsences = Math.max(0, activeEmployees - todayCheckIns);

        BigDecimal overtimeHours = timekeepingRepository
                .sumOvertimeHoursByStatusAndDateRange(firstDayOfMonth, today, Timekeeping.Status.Confirmed);

        return AdminDashboardSummaryResponse.builder()
                .totalEmployees(Math.toIntExact(totalEmployees))
                .activeEmployees(Math.toIntExact(activeEmployees))
                .inactiveEmployees(Math.toIntExact(totalEmployees - activeEmployees))
                .totalDepartments(Math.toIntExact(departmentRepository.count()))
                .pendingTimekeepingRecords(Math.toIntExact(pendingTimekeeping))
                .pendingLeaveRequests(Math.toIntExact(pendingLeaveRequests))
                .overtimeHoursThisMonth(overtimeHours == null ? BigDecimal.ZERO : overtimeHours)
                .todayCheckIns(Math.toIntExact(todayCheckIns))
                .todayAbsences(Math.toIntExact(todayAbsences))
                .build();
    }

    public TimekeepingOverviewResponse getTimekeepingOverview(String range, LocalDate startDate, LocalDate endDate) {
        DateRange dateRange = resolveRange(range, startDate, endDate);
        List<Timekeeping> records = timekeepingRepository.findByWorkDateBetween(
                dateRange.start(),
                dateRange.end());

        Map<LocalDate, TimekeepingOverviewResponse.DailyStatusBreakdown> breakdownMap = new LinkedHashMap<>();
        for (LocalDate date = dateRange.start(); !date.isAfter(dateRange.end()); date = date.plusDays(1)) {
            breakdownMap.put(date, TimekeepingOverviewResponse.DailyStatusBreakdown.builder()
                    .date(date)
                    .pending(0L)
                    .confirmed(0L)
                    .error(0L)
                    .build());
        }

        long totalPending = 0;
        long totalConfirmed = 0;
        long totalError = 0;

        for (Timekeeping record : records) {
            LocalDate workDate = record.getWorkDate();
            if (workDate == null || !breakdownMap.containsKey(workDate)) {
                continue;
            }
            TimekeepingOverviewResponse.DailyStatusBreakdown daily = breakdownMap.get(workDate);
            if (record.getStatus() == Timekeeping.Status.Pending) {
                daily.setPending(daily.getPending() + 1);
                totalPending++;
            } else if (record.getStatus() == Timekeeping.Status.Confirmed) {
                daily.setConfirmed(daily.getConfirmed() + 1);
                totalConfirmed++;
            } else if (record.getStatus() == Timekeeping.Status.Error) {
                daily.setError(daily.getError() + 1);
                totalError++;
            }
        }

        return TimekeepingOverviewResponse.builder()
                .rangeStart(dateRange.start())
                .rangeEnd(dateRange.end())
                .totalPending(totalPending)
                .totalConfirmed(totalConfirmed)
                .totalError(totalError)
                .dailyBreakdown(new ArrayList<>(breakdownMap.values()))
                .build();
    }

    public TimekeepingOvertimeTrendResponse getOvertimeTrend(String range,
                                                             LocalDate startDate,
                                                             LocalDate endDate,
                                                             Integer departmentId) {
        DateRange dateRange = resolveRange(range, startDate, endDate);
        List<Timekeeping> records = timekeepingRepository.findByWorkDateBetweenAndDepartment(
                dateRange.start(),
                dateRange.end(),
                departmentId);

        Map<LocalDate, BigDecimal> overtimeByDay = new LinkedHashMap<>();
        for (LocalDate date = dateRange.start(); !date.isAfter(dateRange.end()); date = date.plusDays(1)) {
            overtimeByDay.put(date, BigDecimal.ZERO);
        }

        BigDecimal totalOvertime = BigDecimal.ZERO;
        for (Timekeeping record : records) {
            if (record.getStatus() != Timekeeping.Status.Confirmed) {
                continue;
            }
            BigDecimal overtime = record.getOvertimeHours() == null ? BigDecimal.ZERO : record.getOvertimeHours();
            LocalDate date = record.getWorkDate();
            if (date == null || !overtimeByDay.containsKey(date)) {
                continue;
            }
            overtimeByDay.put(date, overtimeByDay.get(date).add(overtime));
            totalOvertime = totalOvertime.add(overtime);
        }

        String departmentName = null;
        if (departmentId != null) {
            departmentName = departmentRepository.findById(departmentId)
                    .map(com.example.hrms.entity.Department::getDepartmentName)
                    .orElse(null);
        }

        List<TimekeepingOvertimeTrendResponse.OvertimePoint> points = overtimeByDay.entrySet().stream()
                .map(entry -> TimekeepingOvertimeTrendResponse.OvertimePoint.builder()
                        .date(entry.getKey())
                        .overtimeHours(entry.getValue())
                        .build())
                .collect(Collectors.toList());

        return TimekeepingOvertimeTrendResponse.builder()
                .rangeStart(dateRange.start())
                .rangeEnd(dateRange.end())
                .departmentId(departmentId)
                .departmentName(departmentName)
                .totalOvertimeHours(totalOvertime)
                .points(points)
                .build();
    }

    public LeaveOverviewResponse getLeaveOverview(String range, LocalDate startDate, LocalDate endDate) {
        DateRange dateRange = resolveRange(range, startDate, endDate);
        List<OnLeave> leaves = onLeaveRepository.findByDateRange(
                dateRange.start(),
                dateRange.end(),
                null);

        Map<LocalDate, LeaveOverviewResponse.LeaveDailySummary> breakdown = new LinkedHashMap<>();
        for (LocalDate date = dateRange.start(); !date.isAfter(dateRange.end()); date = date.plusDays(1)) {
            breakdown.put(date, LeaveOverviewResponse.LeaveDailySummary.builder()
                    .date(date)
                    .pending(0)
                    .approved(0)
                    .rejected(0)
                    .build());
        }

        int pending = 0;
        int approved = 0;
        int rejected = 0;
        Set<Integer> uniqueEmployees = new HashSet<>();

        for (OnLeave leave : leaves) {
            if (leave.getEmployee() != null && leave.getEmployee().getEmployeeId() != null) {
                uniqueEmployees.add(leave.getEmployee().getEmployeeId());
            }
            if (leave.getStatus() == OnLeave.Status.Pending) {
                pending++;
            } else if (leave.getStatus() == OnLeave.Status.Approved) {
                approved++;
            } else if (leave.getStatus() == OnLeave.Status.Rejected) {
                rejected++;
            }

            LocalDate effectiveStart = leave.getFromDate();
            LocalDate effectiveEnd = leave.getToDate();
            if (effectiveStart == null || effectiveEnd == null) {
                continue;
            }
            if (effectiveStart.isBefore(dateRange.start())) {
                effectiveStart = dateRange.start();
            }
            if (effectiveEnd.isAfter(dateRange.end())) {
                effectiveEnd = dateRange.end();
            }

            for (LocalDate date = effectiveStart; !date.isAfter(effectiveEnd); date = date.plusDays(1)) {
                LeaveOverviewResponse.LeaveDailySummary daily = breakdown.get(date);
                if (daily == null) {
                    continue;
                }
                switch (leave.getStatus()) {
                    case Pending -> daily.setPending(daily.getPending() + 1);
                    case Approved -> daily.setApproved(daily.getApproved() + 1);
                    case Rejected -> daily.setRejected(daily.getRejected() + 1);
                }
            }
        }

        return LeaveOverviewResponse.builder()
                .rangeStart(dateRange.start())
                .rangeEnd(dateRange.end())
                .totalRequests(leaves.size())
                .pendingRequests(pending)
                .approvedRequests(approved)
                .rejectedRequests(rejected)
                .uniqueEmployees(uniqueEmployees.size())
                .dailyBreakdown(new ArrayList<>(breakdown.values()))
                .build();
    }

    public List<HolidayResponse> getUpcomingHolidays(int limit) {
        return holidayCalendarRepository
                .findByHolidayDateGreaterThanEqual(LocalDate.now(),
                        PageRequest.of(0, limit, Sort.by(Sort.Direction.ASC, "holidayDate")))
                .stream()
                .map(holiday -> HolidayResponse.builder()
                        .holidayId(holiday.getHolidayId())
                        .holidayName(holiday.getHolidayName())
                        .holidayDate(holiday.getHolidayDate())
                        .createdAt(holiday.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    public ScheduleCoverageResponse getScheduleCoverage(String range, LocalDate startDate, LocalDate endDate) {
        DateRange dateRange = resolveRange(range, startDate, endDate);
        List<WorkSchedule> schedules = workScheduleRepository.findByDateRange(
                dateRange.start(),
                dateRange.end());

        int availableEmployees = Math.toIntExact(employeeRepository.countByStatus(Employee.EmploymentStatus.Active));
        Map<LocalDate, DayAccumulator> accumulators = new LinkedHashMap<>();
        for (LocalDate date = dateRange.start(); !date.isAfter(dateRange.end()); date = date.plusDays(1)) {
            accumulators.put(date, new DayAccumulator());
        }

        Set<Integer> uniqueDepartments = new HashSet<>();
        Set<Integer> uniqueEmployees = new HashSet<>();

        for (WorkSchedule schedule : schedules) {
            LocalDate date = schedule.getWorkDate();
            if (date == null || !accumulators.containsKey(date)) {
                continue;
            }
            DayAccumulator accumulator = accumulators.get(date);
            accumulator.events++;

            if (schedule.getHostDepartment() != null) {
                Integer deptId = schedule.getHostDepartment().getDepartmentId();
                accumulator.departments.add(deptId);
                uniqueDepartments.add(deptId);
            }
            if (schedule.getHostEmployee() != null && schedule.getHostEmployee().getEmployeeId() != null) {
                Integer empId = schedule.getHostEmployee().getEmployeeId();
                accumulator.employees.add(empId);
                uniqueEmployees.add(empId);
            }

            if (schedule.getDepartments() != null) {
                for (WorkScheduleDepartment dept : schedule.getDepartments()) {
                    if (dept.getDepartment() != null) {
                        Integer deptId = dept.getDepartment().getDepartmentId();
                        accumulator.departments.add(deptId);
                        uniqueDepartments.add(deptId);
                    }
                }
            }

            if (schedule.getEmployees() != null) {
                for (WorkScheduleEmployee emp : schedule.getEmployees()) {
                    if (emp.getEmployee() != null && emp.getEmployee().getEmployeeId() != null) {
                        Integer empId = emp.getEmployee().getEmployeeId();
                        accumulator.employees.add(empId);
                        uniqueEmployees.add(empId);
                    }
                }
            }
        }

        List<ScheduleCoverageResponse.DayCoverage> dayCoverages = new ArrayList<>();
        BigDecimal totalCoverageRatio = BigDecimal.ZERO;
        for (Map.Entry<LocalDate, DayAccumulator> entry : accumulators.entrySet()) {
            DayAccumulator accumulator = entry.getValue();
            int scheduledEmployees = accumulator.employees.size();
            int shortage = Math.max(0, availableEmployees - scheduledEmployees);
            BigDecimal coverageRatio = availableEmployees == 0
                    ? BigDecimal.ZERO
                    : BigDecimal.valueOf(scheduledEmployees)
                            .divide(BigDecimal.valueOf(availableEmployees), 2, RoundingMode.HALF_UP);
            totalCoverageRatio = totalCoverageRatio.add(coverageRatio);

            dayCoverages.add(ScheduleCoverageResponse.DayCoverage.builder()
                    .date(entry.getKey())
                    .totalEvents(accumulator.events)
                    .departmentsInvolved(accumulator.departments.size())
                    .employeesScheduled(scheduledEmployees)
                    .availableEmployees(availableEmployees)
                    .shortage(shortage)
                    .coverageRatio(coverageRatio)
                    .build());
        }

        BigDecimal averageCoverageRatio = dayCoverages.isEmpty()
                ? BigDecimal.ZERO
                : totalCoverageRatio.divide(BigDecimal.valueOf(dayCoverages.size()), 2, RoundingMode.HALF_UP);

        return ScheduleCoverageResponse.builder()
                .rangeStart(dateRange.start())
                .rangeEnd(dateRange.end())
                .totalSchedules(schedules.size())
                .uniqueDepartments(uniqueDepartments.size())
                .uniqueEmployees(uniqueEmployees.size())
                .averageCoverageRatio(averageCoverageRatio)
                .dailyCoverage(dayCoverages)
                .build();
    }

    public EmployeeAlertResponse getEmployeeAlerts(int limit) {
        LocalDate today = LocalDate.now();
        List<EmployeeAlertResponse.AlertItem> alerts = new ArrayList<>();

        // Employees currently inactive/resigned
        List<Employee> inactiveEmployees = new ArrayList<>();
        inactiveEmployees.addAll(employeeRepository.findByStatus(Employee.EmploymentStatus.Inactive));
        inactiveEmployees.addAll(employeeRepository.findByStatus(Employee.EmploymentStatus.Resigned));

        for (Employee employee : inactiveEmployees) {
            alerts.add(EmployeeAlertResponse.AlertItem.builder()
                    .employeeId(employee.getEmployeeId())
                    .employeeName(employee.getFullName())
                    .employeeEmail(employee.getEmail())
                    .departmentName(employee.getDepartment() != null ? employee.getDepartment().getDepartmentName() : null)
                    .alertType("STATUS")
                    .message("Nhân sự không còn ở trạng thái Active")
                    .build());
        }

        // Employees on long leave (>=5 days) overlapping today
        List<OnLeave> approvedLeaves = onLeaveRepository.findByDateRange(
                today.minusWeeks(2),
                today.plusWeeks(8),
                OnLeave.Status.Approved);
        for (OnLeave leave : approvedLeaves) {
            if (leave.getFromDate() == null || leave.getToDate() == null) {
                continue;
            }
            boolean currentlyOnLeave = !today.isBefore(leave.getFromDate()) && !today.isAfter(leave.getToDate());
            long duration = ChronoUnit.DAYS.between(leave.getFromDate(), leave.getToDate()) + 1;
            if (currentlyOnLeave && duration >= 5 && leave.getEmployee() != null) {
                alerts.add(EmployeeAlertResponse.AlertItem.builder()
                        .employeeId(leave.getEmployee().getEmployeeId())
                        .employeeName(leave.getEmployee().getFullName())
                        .employeeEmail(leave.getEmployee().getEmail())
                        .departmentName(leave.getEmployee().getDepartment() != null
                                ? leave.getEmployee().getDepartment().getDepartmentName()
                                : null)
                        .alertType("LONG_LEAVE")
                        .message(String.format("Nghỉ dài ngày (%d ngày)", duration))
                        .fromDate(leave.getFromDate())
                        .toDate(leave.getToDate())
                        .build());
            }
        }

        // Pending leave requests older than 5 days
        for (OnLeave leave : onLeaveRepository.findByStatus(OnLeave.Status.Pending)) {
            if (leave.getCreatedAt() == null || leave.getEmployee() == null) {
                continue;
            }
            long pendingDays = ChronoUnit.DAYS.between(leave.getCreatedAt().toLocalDate(), today);
            if (pendingDays >= 5) {
                alerts.add(EmployeeAlertResponse.AlertItem.builder()
                        .employeeId(leave.getEmployee().getEmployeeId())
                        .employeeName(leave.getEmployee().getFullName())
                        .employeeEmail(leave.getEmployee().getEmail())
                        .departmentName(leave.getEmployee().getDepartment() != null
                                ? leave.getEmployee().getDepartment().getDepartmentName()
                                : null)
                        .alertType("PENDING_LEAVE")
                        .message(String.format("Đơn nghỉ chờ duyệt %d ngày", pendingDays))
                        .fromDate(leave.getFromDate())
                        .toDate(leave.getToDate())
                        .build());
            }
        }

        List<EmployeeAlertResponse.AlertItem> limitedAlerts = alerts.stream()
                .sorted(Comparator.comparing(EmployeeAlertResponse.AlertItem::getFromDate,
                        Comparator.nullsLast(LocalDate::compareTo)))
                .limit(limit)
                .collect(Collectors.toList());

        return EmployeeAlertResponse.builder()
                .alerts(limitedAlerts)
                .build();
    }

    public List<TimekeepingResponse> getPendingTimekeeping(int limit) {
        return timekeepingRepository.findByStatus(Timekeeping.Status.Pending,
                        PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "workDate", "createdAt")))
                .map(timekeepingService::toResponse)
                .getContent();
    }
    
    public List<TopOvertimeEmployeeResponse> getTopOvertimeEmployees(int limit) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(29);
        List<Object[]> rows = timekeepingRepository.findTopOvertimeEmployees(
                Timekeeping.Status.Confirmed,
                startDate,
                endDate,
                PageRequest.of(0, limit));
        
        return rows.stream()
                .map(row -> TopOvertimeEmployeeResponse.builder()
                        .employeeId((Integer) row[0])
                        .employeeName((String) row[1])
                        .employeeEmail((String) row[2])
                        .departmentName((String) row[3])
                        .overtimeHours(row[4] instanceof BigDecimal
                                ? (BigDecimal) row[4]
                                : row[4] instanceof Number
                                    ? BigDecimal.valueOf(((Number) row[4]).doubleValue())
                                    : BigDecimal.ZERO)
                        .build())
                .collect(Collectors.toList());
    }

    private DateRange resolveRange(String range, LocalDate startDate, LocalDate endDate) {
        LocalDate today = LocalDate.now();
        if (startDate != null || endDate != null) {
            LocalDate effectiveEnd = endDate != null ? endDate : today;
            LocalDate effectiveStart = startDate != null ? startDate : effectiveEnd.minusDays(6);
            if (effectiveStart.isAfter(effectiveEnd)) {
                throw new RuntimeException("startDate cannot be after endDate");
            }
            return new DateRange(effectiveStart, effectiveEnd);
        }

        String normalizedRange = range == null ? "7d" : range.trim().toLowerCase();
        LocalDate effectiveEnd = today;
        LocalDate effectiveStart = switch (normalizedRange) {
            case "7d" -> effectiveEnd.minusDays(6);
            case "14d" -> effectiveEnd.minusDays(13);
            case "30d" -> effectiveEnd.minusDays(29);
            case "90d", "quarter" -> effectiveEnd.minusDays(89);
            case "month" -> effectiveEnd.withDayOfMonth(1);
            case "year" -> effectiveEnd.withDayOfYear(1);
            case "custom" ->
                    throw new RuntimeException("startDate and endDate are required for custom range");
            default -> effectiveEnd.minusDays(6);
        };

        if (effectiveStart.isAfter(effectiveEnd)) {
            throw new RuntimeException("Resolved start date cannot be after end date");
        }
        return new DateRange(effectiveStart, effectiveEnd);
    }

    private record DateRange(LocalDate start, LocalDate end) {}

    private static class DayAccumulator {
        private int events = 0;
        private final Set<Integer> departments = new HashSet<>();
        private final Set<Integer> employees = new HashSet<>();
    }
}


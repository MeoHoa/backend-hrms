package com.example.hrms.service;

import com.example.hrms.entity.Employee;
import com.example.hrms.entity.HolidayCalendar;
import com.example.hrms.entity.Timekeeping;
import com.example.hrms.entity.User;
import com.example.hrms.repository.EmployeeRepository;
import com.example.hrms.repository.HolidayCalendarRepository;
import com.example.hrms.repository.TimekeepingRepository;
import com.example.hrms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import com.example.hrms.dto.request.*;
import com.example.hrms.dto.response.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class TimekeepingService {
    private final TimekeepingRepository timekeepingRepository;
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final HolidayCalendarRepository holidayCalendarRepository;

    // Default work hours (standard working hours)
    private static final BigDecimal DEFAULT_WORK_HOURS = BigDecimal.valueOf(8);
    private static final LocalTime DEFAULT_CHECK_IN = LocalTime.of(8, 0);
    private static final LocalTime DEFAULT_CHECK_OUT = LocalTime.of(17, 0);

    private Employee getCurrentEmployee() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
        if (user.getEmployee() == null) {
            throw new RuntimeException("Current user does not have an associated employee");
        }
        return user.getEmployee();
    }

    /**
     * Set expected times and required hours using default values
     * Note: WorkSchedule is for events/meetings, not daily work schedule
     * For daily work schedule, we use default values (8:00-17:00, 8 hours)
     * TODO: In future, can get from Employee settings or Department settings
     */
    private void setExpectedTimes(Timekeeping timekeeping) {
        timekeeping.setExpectedCheckInTime(DEFAULT_CHECK_IN);
        timekeeping.setExpectedCheckOutTime(DEFAULT_CHECK_OUT);
        timekeeping.setRequiredWorkHours(DEFAULT_WORK_HOURS);
    }

    /**
     * Check if a date is a holiday and return the holiday name
     * Returns null if not a holiday
     */
    private String getHolidayName(LocalDate date) {
        return holidayCalendarRepository.findByHolidayDate(date)
                .map(HolidayCalendar::getHolidayName)
                .orElse(null);
    }

    /**
     * Check if a date is a holiday
     */
    private boolean isHoliday(LocalDate date) {
        return holidayCalendarRepository.existsByHolidayDate(date);
    }

    /**
     * Check if a date is a weekend (Saturday or Sunday)
     */
    private boolean isWeekend(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
    }

    /**
     * Check if a date is a holiday or weekend
     * Returns true if the date is either a holiday or weekend
     */
    private boolean isHolidayOrWeekend(LocalDate date) {
        return isHoliday(date) || isWeekend(date);
    }

    @Transactional
    public TimekeepingResponse checkIn(CheckInRequest request) {
        Employee employee = getCurrentEmployee();
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        // Check if today is a holiday or weekend
        String holidayName = getHolidayName(today);
        boolean isHoliday = holidayName != null;
        boolean isWeekend = isWeekend(today);
        boolean isHolidayOrWeekend = isHolidayOrWeekend(today);

        // Determine status: Always Pending, even for holidays/weekends
        Timekeeping.Status initialStatus = Timekeeping.Status.Pending;

        // Check if there's already a check-in for today
        Optional<Timekeeping> existing = timekeepingRepository.findByEmployeeAndWorkDate(employee, today);
        if (existing.isPresent()) {
            Timekeeping existingRecord = existing.get();
            if (existingRecord.getCheckIn() != null && existingRecord.getCheckOut() == null) {
                throw new RuntimeException("You have already checked in today. Please check out first.");
            }
            // If there's a record but with check-out, update check-in time
            existingRecord.setCheckIn(now);
            if (request != null && request.getReason() != null && !request.getReason().trim().isEmpty()) {
                existingRecord.setReason(request.getReason());
            }
            // Set holiday name if today is a holiday
            existingRecord.setHolidayName(holidayName);
            // Set status: Always Pending, wait for admin approval
            existingRecord.setStatus(Timekeeping.Status.Pending);
            existingRecord.setUpdatedAt(LocalDateTime.now());

            // Set expected times (default values)
            setExpectedTimes(existingRecord);

            existingRecord = timekeepingRepository.save(existingRecord);
            return mapToResponse(existingRecord);
        }

        // Create new timekeeping record
        Timekeeping timekeeping = Timekeeping.builder()
                .employee(employee)
                .checkIn(now)
                .workDate(today)
                .holidayName(holidayName) // Set holiday name if today is a holiday
                .status(initialStatus) // Always Pending
                .workHours(BigDecimal.ZERO)
                .overtimeHours(BigDecimal.ZERO)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Set reason if provided (UI controls)
        // If it's a holiday/weekend and no reason provided, suggest a default reason
        if (request != null && request.getReason() != null && !request.getReason().trim().isEmpty()) {
            timekeeping.setReason(request.getReason());
        } else if (isHoliday) {
            timekeeping.setReason("Làm thêm giờ vào ngày nghỉ lễ");
        } else if (isWeekend) {
            timekeeping.setReason("Làm thêm giờ vào cuối tuần");
        }

        // Set expected times (default values)
        setExpectedTimes(timekeeping);

        timekeeping = timekeepingRepository.save(timekeeping);
        return mapToResponse(timekeeping);
    }

    @Transactional
    public TimekeepingResponse checkOut(CheckOutRequest request) {
        Employee employee = getCurrentEmployee();
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        Timekeeping timekeeping = timekeepingRepository.findByEmployeeAndWorkDate(employee, today)
                .orElseThrow(() -> new RuntimeException("No check-in found for today. Please check in first."));

        // Check if today is a holiday or weekend (check from database, not just from
        // record)
        String holidayName = getHolidayName(today);
        boolean isHoliday = holidayName != null;
        boolean isWeekendDay = isWeekend(today);
        boolean isHolidayOrWeekend = isHoliday || isWeekendDay;

        // Set holiday name if today is a holiday
        if (isHoliday) {
            timekeeping.setHolidayName(holidayName);
        }

        timekeeping.setCheckOut(now);

        // Set reason if provided (UI controls)
        if (request != null && request.getReason() != null && !request.getReason().trim().isEmpty()) {
            // If there's already a reason (from check-in), append or replace
            if (timekeeping.getReason() != null && !timekeeping.getReason().trim().isEmpty()) {
                timekeeping.setReason(timekeeping.getReason() + "; " + request.getReason());
            } else {
                timekeeping.setReason(request.getReason());
            }
        }

        // Calculate work hours
        if (timekeeping.getCheckIn() != null) {
            long minutes = ChronoUnit.MINUTES.between(timekeeping.getCheckIn(), now);
            BigDecimal totalHours = BigDecimal.valueOf(minutes)
                    .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);

            if (isHolidayOrWeekend) {
                // If holiday or weekend: ALL hours are overtime, workHours = 0
                timekeeping.setWorkHours(BigDecimal.ZERO);
                timekeeping.setOvertimeHours(totalHours);

                // Set as Pending for holiday/weekend (require admin approval)
                timekeeping.setStatus(Timekeeping.Status.Pending);

                // Set default reason if not provided
                if (timekeeping.getReason() == null || timekeeping.getReason().trim().isEmpty()) {
                    if (isHoliday) {
                        timekeeping.setReason("Làm thêm giờ vào ngày nghỉ lễ");
                    } else if (isWeekendDay) {
                        timekeeping.setReason("Làm thêm giờ vào cuối tuần");
                    }
                }
            } else {
                // Normal working day: calculate work hours and overtime normally
                BigDecimal requiredHours = timekeeping.getRequiredWorkHours() != null
                        ? timekeeping.getRequiredWorkHours()
                        : DEFAULT_WORK_HOURS;

                if (totalHours.compareTo(requiredHours) > 0) {
                    // More than required hours: required hours = workHours, excess = overtime
                    timekeeping.setWorkHours(requiredHours);
                    timekeeping.setOvertimeHours(totalHours.subtract(requiredHours));
                } else {
                    // Less than or equal to required hours: all are workHours, no overtime
                    timekeeping.setWorkHours(totalHours);
                    timekeeping.setOvertimeHours(BigDecimal.ZERO);
                }

                // Keep status as Pending for normal days (admin needs to approve)
                if (timekeeping.getStatus() == null) {
                    timekeeping.setStatus(Timekeeping.Status.Pending);
                }
            }
        }

        // Set expected times if not already set
        if (timekeeping.getExpectedCheckInTime() == null || timekeeping.getExpectedCheckOutTime() == null) {
            setExpectedTimes(timekeeping);
        }

        timekeeping.setUpdatedAt(LocalDateTime.now());
        timekeeping = timekeepingRepository.save(timekeeping);

        return mapToResponse(timekeeping);
    }

    public TodayStatusResponse getTodayStatus() {
        Employee employee = getCurrentEmployee();
        LocalDate today = LocalDate.now();

        // Check if today is a holiday
        String holidayName = getHolidayName(today);
        boolean isHoliday = holidayName != null;

        Optional<Timekeeping> todayRecord = timekeepingRepository.findByEmployeeAndWorkDate(employee, today);

        if (todayRecord.isEmpty()) {
            // No record today - can check in (even on holidays, for overtime work)
            return TodayStatusResponse.builder()
                    .hasRecord(false)
                    .canCheckIn(true)
                    .canCheckOut(false)
                    .isCompleted(false)
                    .workDate(today)
                    .status(null)
                    .actionNeeded("CHECK_IN")
                    .isHoliday(isHoliday)
                    .holidayName(holidayName)
                    .build();
        }

        Timekeeping record = todayRecord.get();
        boolean hasCheckIn = record.getCheckIn() != null;
        boolean hasCheckOut = record.getCheckOut() != null;
        boolean isCompleted = hasCheckIn && hasCheckOut;
        boolean canCheckOut = hasCheckIn && !hasCheckOut;

        String actionNeeded;
        if (!hasCheckIn) {
            actionNeeded = "CHECK_IN";
        } else if (!hasCheckOut) {
            actionNeeded = "CHECK_OUT";
        } else {
            actionNeeded = "NONE"; // Already completed
        }

        return TodayStatusResponse.builder()
                .hasRecord(true)
                .canCheckIn(!hasCheckIn)
                .canCheckOut(canCheckOut)
                .isCompleted(isCompleted)
                .workDate(today)
                .checkIn(record.getCheckIn())
                .checkOut(record.getCheckOut())
                .status(record.getStatus())
                .actionNeeded(actionNeeded)
                .isHoliday(isHoliday || record.getHolidayName() != null)
                .holidayName(record.getHolidayName() != null ? record.getHolidayName() : holidayName)
                .build();
    }

    public List<TimekeepingResponse> getMyHistory(
            LocalDate startDate,
            LocalDate endDate,
            Integer month,
            Integer year,
            Integer limit) {
        Employee employee = getCurrentEmployee();

        boolean hasRangeFilters = startDate != null || endDate != null || month != null || year != null;
        if (limit != null && limit > 0 && !hasRangeFilters) {
            List<Timekeeping> recent = timekeepingRepository.findRecentByEmployee(
                    employee,
                    PageRequest.of(0, Math.min(limit, 50)));
            return recent.stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        }

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
        } else {
            // Use provided startDate/endDate or defaults
            if (startDate == null) {
                startDate = LocalDate.now().minusMonths(1); // Default to last month
            }
            if (endDate == null) {
                endDate = LocalDate.now(); // Default to today
            }
        }

        List<Timekeeping> records = timekeepingRepository.findByEmployeeAndWorkDateBetween(employee, startDate,
                endDate);
        if (limit != null && limit > 0) {
            return records.stream()
                    .limit(limit)
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        }

        return records.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public Page<TimekeepingResponse> getPendingTimekeeping(Pageable pageable) {
        Page<Timekeeping> pendingRecords = timekeepingRepository.findByStatus(Timekeeping.Status.Pending, pageable);
        return pendingRecords.map(this::mapToResponse);
    }

    public TimekeepingResponse getTimekeepingById(Integer recordId) {
        Timekeeping timekeeping = timekeepingRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Timekeeping record not found with id: " + recordId));
        return mapToResponse(timekeeping);
    }

    @Transactional
    public TimekeepingResponse approveTimekeeping(Integer recordId, ApproveTimekeepingRequest request) {
        Timekeeping timekeeping = timekeepingRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Timekeeping record not found with id: " + recordId));

        if (timekeeping.getStatus() != Timekeeping.Status.Pending) {
            throw new RuntimeException("Only pending records can be approved");
        }

        timekeeping.setStatus(Timekeeping.Status.Confirmed);
        if (request != null && request.getAdminNote() != null && !request.getAdminNote().trim().isEmpty()) {
            // Append to existing admin note if exists
            if (timekeeping.getAdminNote() != null && !timekeeping.getAdminNote().trim().isEmpty()) {
                timekeeping.setAdminNote(timekeeping.getAdminNote() + "\n" + request.getAdminNote());
            } else {
                timekeeping.setAdminNote(request.getAdminNote());
            }
        }
        timekeeping.setUpdatedAt(LocalDateTime.now());
        timekeeping = timekeepingRepository.save(timekeeping);

        return mapToResponse(timekeeping);
    }

    @Transactional
    public List<TimekeepingResponse> batchApproveTimekeeping(BatchApproveRequest request) {
        List<TimekeepingResponse> results = new java.util.ArrayList<>();

        for (Integer recordId : request.getRecordIds()) {
            try {
                Timekeeping timekeeping = timekeepingRepository.findById(recordId)
                        .orElseThrow(() -> new RuntimeException("Timekeeping record not found with id: " + recordId));

                if (timekeeping.getStatus() != Timekeeping.Status.Pending) {
                    log.warn("Skipping record {} - not in Pending status", recordId);
                    continue;
                }

                timekeeping.setStatus(Timekeeping.Status.Confirmed);
                if (request.getAdminNote() != null && !request.getAdminNote().trim().isEmpty()) {
                    if (timekeeping.getAdminNote() != null && !timekeeping.getAdminNote().trim().isEmpty()) {
                        timekeeping.setAdminNote(timekeeping.getAdminNote() + "\n" + request.getAdminNote());
                    } else {
                        timekeeping.setAdminNote(request.getAdminNote());
                    }
                }
                timekeeping.setUpdatedAt(LocalDateTime.now());
                timekeeping = timekeepingRepository.save(timekeeping);

                results.add(mapToResponse(timekeeping));
            } catch (Exception e) {
                log.error("Error approving timekeeping record {}: {}", recordId, e.getMessage());
            }
        }

        return results;
    }

    @Transactional
    public List<TimekeepingResponse> batchRejectTimekeeping(BatchRejectRequest request) {
        List<TimekeepingResponse> results = new java.util.ArrayList<>();

        for (Integer recordId : request.getRecordIds()) {
            try {
                Timekeeping timekeeping = timekeepingRepository.findById(recordId)
                        .orElseThrow(() -> new RuntimeException("Timekeeping record not found with id: " + recordId));

                if (timekeeping.getStatus() != Timekeeping.Status.Pending) {
                    log.warn("Skipping record {} - not in Pending status", recordId);
                    continue;
                }

                timekeeping.setStatus(Timekeeping.Status.Error);
                if (request.getReason() != null && !request.getReason().trim().isEmpty()) {
                    timekeeping.setAdminNote(request.getReason());
                }
                timekeeping.setUpdatedAt(LocalDateTime.now());
                timekeeping = timekeepingRepository.save(timekeeping);

                results.add(mapToResponse(timekeeping));
            } catch (Exception e) {
                log.error("Error rejecting timekeeping record {}: {}", recordId, e.getMessage());
            }
        }

        return results;
    }

    @Transactional
    public TimekeepingResponse markTimekeepingAsError(Integer recordId, String reason) {
        Timekeeping timekeeping = timekeepingRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Timekeeping record not found with id: " + recordId));

        timekeeping.setStatus(Timekeeping.Status.Error);
        if (reason != null && !reason.trim().isEmpty()) {
            if (timekeeping.getAdminNote() != null && !timekeeping.getAdminNote().trim().isEmpty()) {
                timekeeping.setAdminNote(timekeeping.getAdminNote() + "\n" + reason);
            } else {
                timekeeping.setAdminNote(reason);
            }
        }
        timekeeping.setUpdatedAt(LocalDateTime.now());
        timekeeping = timekeepingRepository.save(timekeeping);

        return mapToResponse(timekeeping);
    }

    public Page<TimekeepingResponse> getErrorTimekeeping(Pageable pageable) {
        Page<Timekeeping> errorRecords = timekeepingRepository.findByStatus(Timekeeping.Status.Error, pageable);
        return errorRecords.map(this::mapToResponse);
    }

    public TimekeepingStatsResponse getTimekeepingStats() {
        long totalPending = timekeepingRepository.findByStatus(Timekeeping.Status.Pending).size();
        long totalConfirmed = timekeepingRepository.findByStatus(Timekeeping.Status.Confirmed).size();
        long totalError = timekeepingRepository.findByStatus(Timekeeping.Status.Error).size();
        long totalRecords = timekeepingRepository.count();

        return TimekeepingStatsResponse.builder()
                .totalPending(totalPending)
                .totalConfirmed(totalConfirmed)
                .totalError(totalError)
                .totalRecords(totalRecords)
                .build();
    }

    @Transactional
    public TimekeepingResponse rejectTimekeeping(Integer recordId, RejectTimekeepingRequest request) {
        Timekeeping timekeeping = timekeepingRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Timekeeping record not found with id: " + recordId));

        if (timekeeping.getStatus() != Timekeeping.Status.Pending) {
            throw new RuntimeException("Only pending records can be rejected");
        }

        // Update times if corrected times are provided
        if (request.getCorrectedTimeIn() != null) {
            timekeeping.setCheckIn(request.getCorrectedTimeIn());
        }
        if (request.getCorrectedTimeOut() != null) {
            timekeeping.setCheckOut(request.getCorrectedTimeOut());

            // Recalculate work hours if check-out is updated
            if (timekeeping.getCheckIn() != null) {
                long minutes = ChronoUnit.MINUTES.between(timekeeping.getCheckIn(), timekeeping.getCheckOut());
                BigDecimal totalHours = BigDecimal.valueOf(minutes)
                        .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);

                // Check if workDate is a holiday or weekend
                LocalDate workDate = timekeeping.getWorkDate();
                String holidayName = getHolidayName(workDate);
                boolean isHoliday = holidayName != null;
                boolean isWeekendDay = isWeekend(workDate);
                boolean isHolidayOrWeekend = isHoliday || isWeekendDay;

                // Update holiday name if it's a holiday
                if (isHoliday) {
                    timekeeping.setHolidayName(holidayName);
                }

                if (isHolidayOrWeekend) {
                    // If holiday or weekend: ALL hours are overtime, workHours = 0
                    timekeeping.setWorkHours(BigDecimal.ZERO);
                    timekeeping.setOvertimeHours(totalHours);
                } else {
                    // Normal working day: calculate work hours and overtime normally
                    BigDecimal requiredHours = timekeeping.getRequiredWorkHours() != null
                            ? timekeeping.getRequiredWorkHours()
                            : DEFAULT_WORK_HOURS;

                    if (totalHours.compareTo(requiredHours) > 0) {
                        // More than required hours: required hours = workHours, excess = overtime
                        timekeeping.setWorkHours(requiredHours);
                        timekeeping.setOvertimeHours(totalHours.subtract(requiredHours));
                    } else {
                        // Less than or equal to required hours: all are workHours, no overtime
                        timekeeping.setWorkHours(totalHours);
                        timekeeping.setOvertimeHours(BigDecimal.ZERO);
                    }
                }
            }
        }

        timekeeping.setStatus(Timekeeping.Status.Confirmed);
        if (request.getReason() != null) {
            timekeeping.setAdminNote(request.getReason());
        }
        timekeeping.setUpdatedAt(LocalDateTime.now());
        timekeeping = timekeepingRepository.save(timekeeping);

        return mapToResponse(timekeeping);
    }

    public Page<TimekeepingResponse> getAllTimekeeping(
            Timekeeping.Status status,
            LocalDate workDate,
            Integer employeeId,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable) {
        Page<Timekeeping> records = timekeepingRepository.findAllWithFilters(
                status, workDate, employeeId, startDate, endDate, pageable);
        return records.map(this::mapToResponse);
    }

    @Transactional
    public TimekeepingResponse updateTimekeeping(Integer recordId, UpdateTimekeepingRequest request) {
        Timekeeping timekeeping = timekeepingRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Timekeeping record not found with id: " + recordId));

        boolean recalculate = false;

        // Update check-in if provided
        if (request.getCheckIn() != null) {
            timekeeping.setCheckIn(request.getCheckIn());
            recalculate = true;
        }

        // Update check-out if provided
        if (request.getCheckOut() != null) {
            timekeeping.setCheckOut(request.getCheckOut());
            recalculate = true;
        }

        // Update reason if provided (UI controls)
        if (request.getReason() != null) {
            timekeeping.setReason(request.getReason());
        }

        // Update admin note
        if (request.getAdminNote() != null) {
            timekeeping.setAdminNote(request.getAdminNote());
        }

        // Recalculate work hours if check-in or check-out was updated
        if (recalculate && timekeeping.getCheckIn() != null && timekeeping.getCheckOut() != null) {
            long minutes = ChronoUnit.MINUTES.between(timekeeping.getCheckIn(), timekeeping.getCheckOut());
            BigDecimal totalHours = BigDecimal.valueOf(minutes)
                    .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);

            // Check if workDate is a holiday or weekend
            LocalDate workDate = timekeeping.getWorkDate();
            String holidayName = getHolidayName(workDate);
            boolean isHoliday = holidayName != null;
            boolean isWeekendDay = isWeekend(workDate);
            boolean isHolidayOrWeekend = isHoliday || isWeekendDay;

            // Update holiday name if it's a holiday
            if (isHoliday) {
                timekeeping.setHolidayName(holidayName);
            }

            if (isHolidayOrWeekend) {
                // If holiday or weekend: ALL hours are overtime, workHours = 0
                timekeeping.setWorkHours(BigDecimal.ZERO);
                timekeeping.setOvertimeHours(totalHours);

                // Set as Pending for holiday/weekend (require admin approval)
                timekeeping.setStatus(Timekeeping.Status.Pending);
            } else {
                // Normal working day: calculate work hours and overtime normally
                BigDecimal requiredHours = timekeeping.getRequiredWorkHours() != null
                        ? timekeeping.getRequiredWorkHours()
                        : DEFAULT_WORK_HOURS;

                if (totalHours.compareTo(requiredHours) > 0) {
                    // More than required hours: required hours = workHours, excess = overtime
                    timekeeping.setWorkHours(requiredHours);
                    timekeeping.setOvertimeHours(totalHours.subtract(requiredHours));
                } else {
                    // Less than or equal to required hours: all are workHours, no overtime
                    timekeeping.setWorkHours(totalHours);
                    timekeeping.setOvertimeHours(BigDecimal.ZERO);
                }

                // Update status to Pending if it was corrected, so admin can review again
                timekeeping.setStatus(Timekeeping.Status.Pending);
            }
        }

        // Set expected times if not already set
        if (timekeeping.getExpectedCheckInTime() == null || timekeeping.getExpectedCheckOutTime() == null) {
            setExpectedTimes(timekeeping);
        }

        // Only update status to Pending if not holiday/weekend and not already
        // recalculated
        if (!recalculate && timekeeping.getStatus() != Timekeeping.Status.Confirmed) {
            // Check if workDate is a holiday or weekend
            LocalDate workDate = timekeeping.getWorkDate();
            boolean isHolidayOrWeekend = isHolidayOrWeekend(workDate);
            if (!isHolidayOrWeekend) {
                timekeeping.setStatus(Timekeeping.Status.Pending);
            }
        }

        timekeeping.setUpdatedAt(LocalDateTime.now());
        timekeeping = timekeepingRepository.save(timekeeping);

        return mapToResponse(timekeeping);
    }

    /**
     * Update reason for employee's own timekeeping record
     * Employee can only update the reason field, not check-in/check-out times
     */
    @Transactional
    public TimekeepingResponse updateMyTimekeepingReason(Integer recordId, UpdateMyTimekeepingReasonRequest request) {
        Employee employee = getCurrentEmployee();

        // Find the timekeeping record
        Timekeeping timekeeping = timekeepingRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Timekeeping record not found with id: " + recordId));

        // Verify that the record belongs to the current employee
        if (!timekeeping.getEmployee().getEmployeeId().equals(employee.getEmployeeId())) {
            throw new RuntimeException("You can only update your own timekeeping records");
        }

        // Update reason
        timekeeping.setReason(request.getReason());
        timekeeping.setUpdatedAt(LocalDateTime.now());

        // Status should remain Pending if it was Pending, so admin can review
        // If it was already Confirmed, we might want to change it back to Pending for
        // admin review
        if (timekeeping.getStatus() == Timekeeping.Status.Confirmed) {
            timekeeping.setStatus(Timekeeping.Status.Pending);
        }

        timekeeping = timekeepingRepository.save(timekeeping);
        return mapToResponse(timekeeping);
    }

    public TimekeepingResponse toResponse(Timekeeping timekeeping) {
        return mapToResponse(timekeeping);
    }

    private TimekeepingResponse mapToResponse(Timekeeping timekeeping) {
        TimekeepingResponse.TimekeepingResponseBuilder builder = TimekeepingResponse.builder()
                .id(timekeeping.getId())
                .checkIn(timekeeping.getCheckIn())
                .checkOut(timekeeping.getCheckOut())
                .workDate(timekeeping.getWorkDate())
                .requiredWorkHours(timekeeping.getRequiredWorkHours())
                .workHours(timekeeping.getWorkHours())
                .overtimeHours(timekeeping.getOvertimeHours())
                .expectedCheckInTime(timekeeping.getExpectedCheckInTime())
                .expectedCheckOutTime(timekeeping.getExpectedCheckOutTime())
                .reason(timekeeping.getReason())
                .adminNote(timekeeping.getAdminNote())
                .holidayName(timekeeping.getHolidayName())
                .isHoliday(timekeeping.getHolidayName() != null)
                .status(timekeeping.getStatus())
                .createdAt(timekeeping.getCreatedAt())
                .updatedAt(timekeeping.getUpdatedAt());

        if (timekeeping.getEmployee() != null) {
            builder.employeeId(timekeeping.getEmployee().getEmployeeId())
                    .employeeFullName(timekeeping.getEmployee().getFullName())
                    .employeeEmail(timekeeping.getEmployee().getEmail());
        }

        return builder.build();
    }
}

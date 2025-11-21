package com.example.hrms.schedule;

import com.example.hrms.entity.Employee;
import com.example.hrms.entity.HolidayCalendar;
import com.example.hrms.entity.Timekeeping;
import com.example.hrms.repository.EmployeeRepository;
import com.example.hrms.repository.HolidayCalendarRepository;
import com.example.hrms.repository.TimekeepingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * Scheduled task to automatically create timekeeping records for employees
 * who forgot to check in/out on working days (excluding Saturday, Sunday, and holidays)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TimekeepingScheduler {
    private final TimekeepingRepository timekeepingRepository;
    private final EmployeeRepository employeeRepository;
    private final HolidayCalendarRepository holidayCalendarRepository;
    
    private static final BigDecimal DEFAULT_WORK_HOURS = BigDecimal.valueOf(8);
    private static final LocalTime DEFAULT_CHECK_IN = LocalTime.of(8, 0);
    private static final LocalTime DEFAULT_CHECK_OUT = LocalTime.of(17, 0);
    
    /**
     * Runs daily at 23:59 PM to check for employees who didn't check in/out today
     * Cron format: second minute hour day month weekday
     * 0 59 23 * * * = Every day at 23:59:00
     */
    @Scheduled(cron = "0 59 23 * * *")
    @Transactional
    public void createMissingTimekeepingRecords() {
        LocalDate today = LocalDate.now();
        log.info("Running scheduled task to create missing timekeeping records for date: {}", today);
        
        // Check if today is Saturday or Sunday
        if (isWeekend(today)) {
            log.info("Today ({}) is weekend, skipping timekeeping record creation", today);
            return;
        }
        
        // Check if today is a holiday (you can extend this logic later)
        if (isHoliday(today)) {
            log.info("Today ({}) is a holiday, skipping timekeeping record creation", today);
            return;
        }
        
        // Get all active employees
        List<Employee> activeEmployees = employeeRepository.findAll().stream()
                .filter(e -> e.getStatus() == Employee.EmploymentStatus.Active)
                .collect(java.util.stream.Collectors.toList());
        
        log.info("Found {} active employees to check", activeEmployees.size());
        
        int createdCount = 0;
        
        for (Employee employee : activeEmployees) {
            try {
                // Check if timekeeping record already exists for today
                Optional<Timekeeping> existingRecord = timekeepingRepository
                        .findByEmployeeAndWorkDate(employee, today);
                
                if (existingRecord.isPresent()) {
                    // Record already exists, skip
                    continue;
                }
                
                // Get schedule for this employee on today

                // Get holiday name if today is a holiday (should be null since we already checked)
                String holidayName = getHolidayName(today);
                
                // Create new timekeeping record with status Pending
                Timekeeping timekeeping = Timekeeping.builder()
                        .employee(employee)
                        .checkIn(null) // No check-in
                        .checkOut(null) // No check-out
                        .workDate(today)
                        .holidayName(holidayName) // Set holiday name if today is a holiday (should be null)
                        .status(Timekeeping.Status.Pending)
                        .workHours(BigDecimal.ZERO)
                        .overtimeHours(BigDecimal.ZERO)
                        .adminNote("Không thực hiện chấm công")
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();
                
                // Set expected times and required hours from schedule
                setExpectedTimes(timekeeping);
                
                timekeepingRepository.save(timekeeping);
                createdCount++;
                
                log.debug("Created missing timekeeping record for employee {} on {}", 
                        employee.getFullName(), today);
                        
            } catch (Exception e) {
                log.error("Error creating timekeeping record for employee {} on {}: {}", 
                        employee.getFullName(), today, e.getMessage());
            }
        }
        
        log.info("Scheduled task completed. Created {} missing timekeeping records for {}", 
                createdCount, today);
    }
    
    /**
     * Check if the date is a weekend (Saturday or Sunday)
     */
    private boolean isWeekend(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
    }
    
    /**
     * Check if the date is a holiday
     * Returns the holiday name if it's a holiday, null otherwise
     */
    private String getHolidayName(LocalDate date) {
        return holidayCalendarRepository.findByHolidayDate(date)
                .map(HolidayCalendar::getHolidayName)
                .orElse(null);
    }
    
    /**
     * Check if the date is a holiday
     */
    private boolean isHoliday(LocalDate date) {
        return holidayCalendarRepository.existsByHolidayDate(date);
    }
    
    /**
     * Set expected times and required hours from schedule
     */
    private void setExpectedTimes(Timekeeping timekeeping) {
        timekeeping.setExpectedCheckInTime(DEFAULT_CHECK_IN);
        timekeeping.setExpectedCheckOutTime(DEFAULT_CHECK_OUT);
        timekeeping.setRequiredWorkHours(DEFAULT_WORK_HOURS);
    }
}


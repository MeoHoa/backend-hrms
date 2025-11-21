package com.example.hrms.repository;

import com.example.hrms.entity.Employee;
import com.example.hrms.entity.WorkSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface WorkScheduleRepository extends JpaRepository<WorkSchedule, Integer> {
    
    // Find schedules by employee (participating or hosting) and month/year
    @Query("SELECT DISTINCT w FROM WorkSchedule w " +
           "LEFT JOIN w.employees e " +
           "WHERE (w.hostEmployee = :employee OR e.employee = :employee) " +
           "AND YEAR(w.workDate) = :year " +
           "AND MONTH(w.workDate) = :month " +
           "ORDER BY w.workDate ASC, w.fromHour ASC")
    List<WorkSchedule> findByEmployeeAndMonth(@Param("employee") Employee employee,
                                               @Param("year") int year,
                                               @Param("month") int month);
    
    // Find schedules by department (host department or participating departments) and date
    @Query("SELECT DISTINCT w FROM WorkSchedule w " +
           "LEFT JOIN w.departments d " +
           "WHERE (:departmentId IS NULL OR w.hostDepartment.departmentId = :departmentId OR d.department.departmentId = :departmentId) " +
           "AND (:date IS NULL OR w.workDate = :date) " +
           "ORDER BY w.workDate ASC, w.fromHour ASC")
    List<WorkSchedule> findByDepartmentAndDate(@Param("departmentId") Integer departmentId,
                                                 @Param("date") LocalDate date);
    
    @Query("SELECT w FROM WorkSchedule w " +
           "WHERE w.workDate BETWEEN :startDate AND :endDate " +
           "ORDER BY w.workDate ASC, w.fromHour ASC")
    List<WorkSchedule> findByDateRange(@Param("startDate") LocalDate startDate,
                                       @Param("endDate") LocalDate endDate);
    
    @Query("SELECT DISTINCT w FROM WorkSchedule w " +
           "LEFT JOIN w.employees e " +
           "WHERE (w.hostEmployee = :employee OR e.employee = :employee) " +
           "AND w.workDate BETWEEN :startDate AND :endDate " +
           "ORDER BY w.workDate ASC, w.fromHour ASC")
    List<WorkSchedule> findByEmployeeAndDateRange(@Param("employee") Employee employee,
                                                  @Param("startDate") LocalDate startDate,
                                                  @Param("endDate") LocalDate endDate);
}


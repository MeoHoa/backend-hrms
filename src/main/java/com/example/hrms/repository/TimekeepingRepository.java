package com.example.hrms.repository;

import com.example.hrms.entity.Employee;
import com.example.hrms.entity.Timekeeping;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface TimekeepingRepository extends JpaRepository<Timekeeping, Integer> {
    
    Optional<Timekeeping> findByEmployeeAndWorkDate(Employee employee, LocalDate workDate);
    
    Optional<Timekeeping> findByEmployeeAndWorkDateAndCheckOutIsNull(Employee employee, LocalDate workDate);
    
    @Query("SELECT t FROM Timekeeping t WHERE t.employee = :employee " +
           "AND t.workDate BETWEEN :startDate AND :endDate " +
           "ORDER BY t.workDate DESC, t.checkIn DESC")
    List<Timekeeping> findByEmployeeAndWorkDateBetween(@Param("employee") Employee employee,
                                                         @Param("startDate") LocalDate startDate,
                                                         @Param("endDate") LocalDate endDate);
    
    @Query("SELECT t FROM Timekeeping t WHERE t.status = :status " +
           "ORDER BY t.workDate DESC, t.createdAt DESC")
    Page<Timekeeping> findByStatus(@Param("status") Timekeeping.Status status, Pageable pageable);
    
    List<Timekeeping> findByStatus(Timekeeping.Status status);
    
    long countByStatus(Timekeeping.Status status);
    
    long countByWorkDateAndCheckInIsNotNull(LocalDate workDate);
    
    @Query("SELECT COALESCE(SUM(t.overtimeHours), 0) FROM Timekeeping t " +
           "WHERE t.workDate BETWEEN :startDate AND :endDate " +
           "AND (:status IS NULL OR t.status = :status)")
    BigDecimal sumOvertimeHoursByStatusAndDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("status") Timekeeping.Status status);
    
    @Query("SELECT t FROM Timekeeping t WHERE t.workDate BETWEEN :startDate AND :endDate " +
           "ORDER BY t.workDate ASC, t.createdAt DESC")
    List<Timekeeping> findByWorkDateBetween(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
    
    @Query("SELECT t FROM Timekeeping t WHERE t.workDate BETWEEN :startDate AND :endDate " +
           "AND (:departmentId IS NULL OR (t.employee.department IS NOT NULL " +
           "AND t.employee.department.departmentId = :departmentId)) " +
           "ORDER BY t.workDate ASC, t.createdAt DESC")
    List<Timekeeping> findByWorkDateBetweenAndDepartment(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("departmentId") Integer departmentId);
    
    @Query("SELECT t FROM Timekeeping t WHERE " +
           "(:status IS NULL OR t.status = :status) " +
           "AND (:workDate IS NULL OR t.workDate = :workDate) " +
           "AND (:employeeId IS NULL OR t.employee.employeeId = :employeeId) " +
           "AND (:startDate IS NULL OR t.workDate >= :startDate) " +
           "AND (:endDate IS NULL OR t.workDate <= :endDate) " +
           "ORDER BY t.workDate DESC, t.createdAt DESC")
    Page<Timekeeping> findAllWithFilters(
            @Param("status") Timekeeping.Status status,
            @Param("workDate") LocalDate workDate,
            @Param("employeeId") Integer employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable);
    
    @Query("SELECT t FROM Timekeeping t WHERE t.employee = :employee " +
           "ORDER BY t.workDate DESC, t.createdAt DESC")
    List<Timekeeping> findRecentByEmployee(
            @Param("employee") Employee employee,
            Pageable pageable);
    
    @Query("SELECT t.employee.employeeId AS employeeId, " +
           "t.employee.fullName AS fullName, " +
           "t.employee.email AS email, " +
           "t.employee.department.departmentName AS departmentName, " +
           "COALESCE(SUM(t.overtimeHours), 0) AS totalOvertime " +
           "FROM Timekeeping t " +
           "WHERE t.status = :status " +
           "AND t.workDate BETWEEN :startDate AND :endDate " +
           "GROUP BY t.employee.employeeId, t.employee.fullName, t.employee.email, t.employee.department.departmentName " +
           "ORDER BY totalOvertime DESC")
    List<Object[]> findTopOvertimeEmployees(
            @Param("status") Timekeeping.Status status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable);
}

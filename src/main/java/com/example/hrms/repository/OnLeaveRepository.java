package com.example.hrms.repository;

import com.example.hrms.entity.Employee;
import com.example.hrms.entity.OnLeave;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface OnLeaveRepository extends JpaRepository<OnLeave, Integer> {
    
    List<OnLeave> findByEmployee(Employee employee);
    
    @Query("SELECT o FROM OnLeave o WHERE o.employee = :employee " +
           "AND (:status IS NULL OR o.status = :status) " +
           "ORDER BY o.createdAt DESC")
    List<OnLeave> findByEmployeeAndStatus(@Param("employee") Employee employee, 
                                           @Param("status") OnLeave.Status status);
    
    @Query("SELECT o FROM OnLeave o WHERE o.employee = :employee " +
           "AND (:status IS NULL OR o.status = :status) " +
           "AND (:startDate IS NULL OR o.toDate >= :startDate) " +
           "AND (:endDate IS NULL OR o.fromDate <= :endDate) " +
           "ORDER BY o.createdAt DESC")
    List<OnLeave> findByEmployeeAndStatusAndDateRange(
            @Param("employee") Employee employee, 
            @Param("status") OnLeave.Status status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
    
    @Query("SELECT o FROM OnLeave o WHERE o.status = :status " +
           "ORDER BY o.createdAt DESC")
    Page<OnLeave> findByStatus(@Param("status") OnLeave.Status status, Pageable pageable);
    
    List<OnLeave> findByStatus(OnLeave.Status status);
    
    long countByStatus(OnLeave.Status status);
    
    @Query("SELECT o FROM OnLeave o WHERE " +
           "(:status IS NULL OR o.status = :status) " +
           "AND (:employeeId IS NULL OR o.employee.employeeId = :employeeId) " +
           "AND (:leaveTypeId IS NULL OR o.leaveType.leavetypeId = :leaveTypeId) " +
           "AND (:startDate IS NULL OR o.toDate >= :startDate) " +
           "AND (:endDate IS NULL OR o.fromDate <= :endDate) " +
           "ORDER BY o.createdAt DESC")
    Page<OnLeave> findAllWithFilters(
            @Param("status") OnLeave.Status status,
            @Param("employeeId") Integer employeeId,
            @Param("leaveTypeId") Integer leaveTypeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable);
    
    @Query("SELECT o FROM OnLeave o WHERE " +
           "o.fromDate <= :endDate AND o.toDate >= :startDate " +
           "AND (:status IS NULL OR o.status = :status)")
    List<OnLeave> findByDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("status") OnLeave.Status status);
}

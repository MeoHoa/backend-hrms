package com.example.hrms.repository;

import com.example.hrms.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Integer> {
    
    long countByStatus(Employee.EmploymentStatus status);
    
    java.util.List<Employee> findByStatus(Employee.EmploymentStatus status);
    
    @Query("SELECT e FROM Employee e " +
           "LEFT JOIN e.department d " +
           "WHERE (:search IS NULL OR :search = '' OR " +
           "LOWER(e.fullName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(e.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(e.phone) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "e.employeeId IN (SELECT u.employee.employeeId FROM User u WHERE " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')))) " +
           "AND (:departmentId IS NULL OR d.departmentId = :departmentId)")
    Page<Employee> searchEmployees(@Param("search") String search, 
                                    @Param("departmentId") Integer departmentId, 
                                    Pageable pageable);
    
    @Query("SELECT e FROM Employee e " +
           "LEFT JOIN FETCH e.department " +
           "WHERE e.employeeId = :id")
    Optional<Employee> findByIdWithDepartment(@Param("id") Integer id);
    
    Optional<Employee> findByPhone(String phone);
    
    @Query("SELECT e FROM Employee e " +
           "WHERE e.department = :department " +
           "ORDER BY e.fullName ASC")
    Page<Employee> findByDepartment(@Param("department") com.example.hrms.entity.Department department, 
                                     Pageable pageable);
    
    @Query("SELECT e FROM Employee e " +
           "WHERE e.department = :department " +
           "AND (:search IS NULL OR :search = '' OR " +
           "LOWER(e.fullName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(e.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(e.phone) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "ORDER BY e.fullName ASC")
    Page<Employee> findByDepartmentAndSearch(@Param("department") com.example.hrms.entity.Department department,
                                              @Param("search") String search,
                                              Pageable pageable);
    
    @Query("SELECT e FROM Employee e " +
           "WHERE e.department IN :departments " +
           "AND (:search IS NULL OR :search = '' OR " +
           "LOWER(e.fullName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(e.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(e.phone) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "ORDER BY e.fullName ASC")
    Page<Employee> findByDepartmentsAndSearch(@Param("departments") java.util.List<com.example.hrms.entity.Department> departments,
                                               @Param("search") String search,
                                               Pageable pageable);
    
    @Query("SELECT e FROM Employee e " +
           "WHERE e.department IN :departments " +
           "ORDER BY e.fullName ASC")
    Page<Employee> findByDepartments(@Param("departments") java.util.List<com.example.hrms.entity.Department> departments,
                                     Pageable pageable);
}

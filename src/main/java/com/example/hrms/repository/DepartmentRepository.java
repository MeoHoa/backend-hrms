package com.example.hrms.repository;

import com.example.hrms.entity.Department;
import com.example.hrms.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DepartmentRepository extends JpaRepository<Department, Integer> {
    List<Department> findByAdmin(Employee admin);
}

package com.example.hrms.repository;

import com.example.hrms.entity.WorkSchedule;
import com.example.hrms.entity.WorkScheduleDepartment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkScheduleDepartmentRepository extends JpaRepository<WorkScheduleDepartment, Integer> {
    List<WorkScheduleDepartment> findByWorkSchedule(WorkSchedule workSchedule);
    void deleteByWorkSchedule(WorkSchedule workSchedule);
}


package com.example.hrms.repository;

import com.example.hrms.entity.WorkSchedule;
import com.example.hrms.entity.WorkScheduleEmployee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkScheduleEmployeeRepository extends JpaRepository<WorkScheduleEmployee, Integer> {
    List<WorkScheduleEmployee> findByWorkSchedule(WorkSchedule workSchedule);
    void deleteByWorkSchedule(WorkSchedule workSchedule);
}


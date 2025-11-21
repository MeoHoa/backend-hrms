package com.example.hrms.repository;

import com.example.hrms.entity.HolidayCalendar;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface HolidayCalendarRepository extends JpaRepository<HolidayCalendar, Integer> {
    
    // Tìm theo năm
    @Query("SELECT h FROM HolidayCalendar h WHERE YEAR(h.holidayDate) = :year ORDER BY h.holidayDate ASC")
    List<HolidayCalendar> findByYear(@Param("year") int year);
    
    // Tìm theo khoảng ngày
    @Query("SELECT h FROM HolidayCalendar h WHERE h.holidayDate BETWEEN :startDate AND :endDate ORDER BY h.holidayDate ASC")
    List<HolidayCalendar> findByDateBetween(@Param("startDate") LocalDate startDate, 
                                             @Param("endDate") LocalDate endDate);
    
    // Tìm theo ngày cụ thể
    Optional<HolidayCalendar> findByHolidayDate(LocalDate holidayDate);
    
    // Kiểm tra xem một ngày có phải là ngày lễ không
    boolean existsByHolidayDate(LocalDate holidayDate);
    
    Page<HolidayCalendar> findByHolidayDateGreaterThanEqual(LocalDate holidayDate, Pageable pageable);
}


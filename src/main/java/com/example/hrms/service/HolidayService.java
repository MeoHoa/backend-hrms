package com.example.hrms.service;

import com.example.hrms.entity.HolidayCalendar;
import com.example.hrms.repository.HolidayCalendarRepository;
import com.example.hrms.dto.request.CreateHolidayRequest;
import com.example.hrms.dto.response.HolidayResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HolidayService {
    private final HolidayCalendarRepository holidayCalendarRepository;

    /**
     * Lấy danh sách ngày nghỉ lễ
     * @param year Năm (optional). Nếu không có, trả về tất cả
     * @return Danh sách ngày nghỉ lễ
     */
    public List<HolidayResponse> getAllHolidays(Integer year) {
        List<HolidayCalendar> holidays;
        
        if (year != null) {
            // Lấy theo năm
            holidays = holidayCalendarRepository.findByYear(year);
        } else {
            // Lấy tất cả
            holidays = holidayCalendarRepository.findAll();
        }
        
        return holidays.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lấy danh sách ngày nghỉ lễ trong khoảng thời gian
     */
    public List<HolidayResponse> getHolidaysByDateRange(LocalDate startDate, LocalDate endDate) {
        List<HolidayCalendar> holidays = holidayCalendarRepository.findByDateBetween(startDate, endDate);
        return holidays.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Kiểm tra một ngày có phải là ngày nghỉ lễ không
     */
    public boolean isHoliday(LocalDate date) {
        return holidayCalendarRepository.existsByHolidayDate(date);
    }

    /**
     * Tạo ngày nghỉ lễ mới
     */
    @Transactional
    public HolidayResponse createHoliday(CreateHolidayRequest request) {
        // Kiểm tra xem ngày đã tồn tại chưa
        if (holidayCalendarRepository.existsByHolidayDate(request.getHolidayDate())) {
            throw new RuntimeException("Holiday date already exists: " + request.getHolidayDate());
        }

        HolidayCalendar holiday = HolidayCalendar.builder()
                .holidayName(request.getHolidayName())
                .holidayDate(request.getHolidayDate())
                .createdAt(LocalDateTime.now())
                .build();

        holiday = holidayCalendarRepository.save(holiday);
        return mapToResponse(holiday);
    }

    /**
     * Xóa ngày nghỉ lễ
     */
    @Transactional
    public void deleteHoliday(Integer holidayId) {
        HolidayCalendar holiday = holidayCalendarRepository.findById(holidayId)
                .orElseThrow(() -> new RuntimeException("Holiday not found with id: " + holidayId));
        
        holidayCalendarRepository.delete(holiday);
    }

    /**
     * Map entity sang response
     */
    private HolidayResponse mapToResponse(HolidayCalendar holiday) {
        return HolidayResponse.builder()
                .holidayId(holiday.getHolidayId())
                .holidayName(holiday.getHolidayName())
                .holidayDate(holiday.getHolidayDate())
                .createdAt(holiday.getCreatedAt())
                .build();
    }
}


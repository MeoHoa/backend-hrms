package com.example.hrms.controller;

import com.example.hrms.dto.base.ApiResponse;
import com.example.hrms.dto.request.CreateHolidayRequest;
import com.example.hrms.dto.response.HolidayResponse;
import com.example.hrms.service.HolidayService;
import com.example.hrms.util.ResponseHelper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/holidays")
@RequiredArgsConstructor
public class HolidayController {
    private final HolidayService holidayService;

    /**
     * Lấy danh sách ngày nghỉ lễ
     * Query params:
     *   - year: Năm (optional). Nếu không có, trả về tất cả
     *   - startDate: Ngày bắt đầu (optional, dùng với endDate)
     *   - endDate: Ngày kết thúc (optional, dùng với startDate)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<HolidayResponse>>> getAllHolidays(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        List<HolidayResponse> holidays;
        
        if (startDate != null && endDate != null) {
            // Lấy theo khoảng ngày
            holidays = holidayService.getHolidaysByDateRange(startDate, endDate);
        } else {
            // Lấy theo năm hoặc tất cả
            holidays = holidayService.getAllHolidays(year);
        }
        
        return ResponseHelper.success("Holidays retrieved successfully", holidays);
    }

    /**
     * Tạo ngày nghỉ lễ mới
     */
    @PostMapping
    public ResponseEntity<ApiResponse<HolidayResponse>> createHoliday(
            @Valid @RequestBody CreateHolidayRequest request) {
        HolidayResponse response = holidayService.createHoliday(request);
        return ResponseHelper.created("Holiday created successfully", response);
    }

    /**
     * Xóa ngày nghỉ lễ
     */
    @DeleteMapping("/{holidayId}")
    public ResponseEntity<ApiResponse<Void>> deleteHoliday(@PathVariable Integer holidayId) {
        holidayService.deleteHoliday(holidayId);
        return ResponseHelper.success("Holiday deleted successfully", null);
    }
}


package com.example.hrms.util;

import com.example.hrms.dto.base.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.example.hrms.dto.request.*;
import com.example.hrms.dto.response.*;
/**
 * Utility class to create standardized API responses
 */
public class ResponseHelper {

    public static <T> ResponseEntity<ApiResponse<T>> success(T data) {
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    public static <T> ResponseEntity<ApiResponse<T>> success(String message, T data) {
        return ResponseEntity.ok(ApiResponse.success(message, data));
    }

    public static <T> ResponseEntity<ApiResponse<T>> success(String message, T data, HttpStatus status) {
        return ResponseEntity.status(status).body(ApiResponse.success(message, data, status.value()));
    }

    public static <T> ResponseEntity<ApiResponse<T>> created(String message, T data) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(message, data, HttpStatus.CREATED.value()));
    }

    public static <T> ResponseEntity<ApiResponse<T>> error(String message, HttpStatus status) {
        return ResponseEntity.status(status)
                .body(ApiResponse.error(message, status.value()));
    }

    public static <T> ResponseEntity<ApiResponse<T>> error(String message, T data, HttpStatus status) {
        return ResponseEntity.status(status)
                .body(ApiResponse.error(message, data, status.value()));
    }

    public static <T> ResponseEntity<ApiResponse<T>> badRequest(String message) {
        return error(message, HttpStatus.BAD_REQUEST);
    }

    public static <T> ResponseEntity<ApiResponse<T>> unauthorized(String message) {
        return error(message, HttpStatus.UNAUTHORIZED);
    }

    public static <T> ResponseEntity<ApiResponse<T>> forbidden(String message) {
        return error(message, HttpStatus.FORBIDDEN);
    }

    public static <T> ResponseEntity<ApiResponse<T>> notFound(String message) {
        return error(message, HttpStatus.NOT_FOUND);
    }
}


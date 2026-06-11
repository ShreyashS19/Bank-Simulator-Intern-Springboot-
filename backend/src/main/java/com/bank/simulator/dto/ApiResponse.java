package com.bank.simulator.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.jsonwebtoken.security.Password;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.commons.collections4.Get;
import org.hibernate.sql.Delete;
import org.hibernate.sql.Update;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;
    private String timestamp;

    // Used when API needs to return => Get Account API
    //                                  Login API
    //                                  Fetch User API
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .build();
    }
   
    // Used when => operation successful
    //              but no data needs to be returned
    // Delete API
    // Update API
    // Password changed
    // OTP sent 
    public static <T> ApiResponse<T> success(String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .timestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .build();
    }
   
    // Used when =>
    //             validation fails
    //             business exception occurs
    //             resource not found
    //             duplicate data
    //             login failed

    //             Mostly used inside =>
    //             GlobalExceptionHandler
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .timestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .build();
    }
}

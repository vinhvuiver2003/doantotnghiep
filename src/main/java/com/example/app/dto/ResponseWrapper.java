package com.example.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponseWrapper<T> {
    private boolean success;
    private String message;
    private T data;
    private LocalDateTime timestamp = LocalDateTime.now();

    public ResponseWrapper(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public ResponseWrapper(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public static <T> ResponseWrapper<T> success(T data) {
        return new ResponseWrapper<>(true, "Operation successful", data);
    }

    public static <T> ResponseWrapper<T> success(String message, T data) {
        return new ResponseWrapper<>(true, message, data);
    }

    public static ResponseWrapper<?> success(String message) {
        return new ResponseWrapper<>(true, message);
    }

    public static ResponseWrapper<?> error(String message) {
        return new ResponseWrapper<>(false, message);
    }
    
    public static <T> ResponseWrapper<T> error(String message, T data) {
        return new ResponseWrapper<>(false, message, data);
    }
    
    public static <T> ResponseWrapper<T> error(String message, Class<T> clazz) {
        return new ResponseWrapper<>(false, message);
    }
}
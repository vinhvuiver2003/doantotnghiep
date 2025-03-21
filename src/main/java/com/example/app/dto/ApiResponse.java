package com.example.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Cấu trúc phản hồi chuẩn của API")
public class ApiResponse<T> {

    @Schema(description = "Trạng thái thành công của request", example = "true")
    private boolean success;

    @Schema(description = "Thông báo từ server", example = "Operation successful")
    private String message;

    @Schema(description = "Dữ liệu trả về")
    private T data;

    @Schema(description = "Thời gian phản hồi")
    private LocalDateTime timestamp = LocalDateTime.now();

    public ApiResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "Operation successful", data);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    public static ApiResponse<?> success(String message) {
        return new ApiResponse<>(true, message);
    }

    public static ApiResponse<?> error(String message) {
        return new ApiResponse<>(false, message);
    }
}
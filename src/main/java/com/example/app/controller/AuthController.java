package com.example.app.controller;
import com.example.app.dto.ApiResponse;
import com.example.app.dto.LoginRequest;
import com.example.app.dto.LoginResponse;
import com.example.app.dto.UserCreateDTO;
import com.example.app.dto.UserDTO;
import com.example.app.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Đăng nhập người dùng
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest loginRequest) {
        LoginResponse loginResponse = authService.login(loginRequest);
        return ResponseEntity.ok(ApiResponse.success("Đăng nhập thành công", loginResponse));
    }

    /**
     * Đăng ký người dùng mới
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserDTO>> register(@Valid @RequestBody UserCreateDTO registerRequest) {
        UserDTO userDTO = authService.register(registerRequest);
        return new ResponseEntity<>(
                ApiResponse.success("Đăng ký thành công", userDTO),
                HttpStatus.CREATED);
    }

    /**
     * Lấy thông tin người dùng hiện tại
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDTO>> getCurrentUser() {
        UserDTO userDTO = authService.getCurrentUser();
        if (userDTO != null) {
            return ResponseEntity.ok(ApiResponse.success("User information retrieved successfully", userDTO));
        } else {
            // Tạo đối tượng ApiResponse<UserDTO> với data là null
            ApiResponse<UserDTO> response = new ApiResponse<>(false, "User not authenticated", null);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    /**
     * Đăng xuất
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<?>> logout() {
        // Xóa Authentication khỏi SecurityContext
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(ApiResponse.success("Đăng xuất thành công"));
    }

    /**
     * Kiểm tra token có hợp lệ không
     */
    @GetMapping("/validate-token")
    public ResponseEntity<ApiResponse<Boolean>> validateToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isValid = authentication != null && authentication.isAuthenticated()
                && !authentication.getPrincipal().equals("anonymousUser");
        return ResponseEntity.ok(ApiResponse.success("Token validation status", isValid));
    }
}
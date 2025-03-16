package com.example.app.controller;


import com.example.app.dto.LoginRequest;
import com.example.app.dto.LoginResponse;
import com.example.app.dto.UserCreateDTO;
import com.example.app.dto.UserDTO;
import com.example.app.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Authentication", description = "API xác thực và quản lý người dùng")
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Đăng nhập người dùng
     */
    @Operation(summary = "Đăng nhập người dùng", description = "API để đăng nhập và lấy JWT token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Đăng nhập thành công",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = LoginResponse.class))),
            @ApiResponse(responseCode = "401", description = "Thông tin đăng nhập không chính xác"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ")
    })
    @PostMapping("/login")
    public ResponseEntity<com.example.app.dto.ApiResponse<LoginResponse>> login(
            @Parameter(description = "Thông tin đăng nhập", required = true)
            @Valid @RequestBody LoginRequest loginRequest) {
        LoginResponse loginResponse = authService.login(loginRequest);
        return ResponseEntity.ok(com.example.app.dto.ApiResponse.success("Đăng nhập thành công", loginResponse));
    }

    /**
     * Đăng ký người dùng mới
     */
    @Operation(summary = "Đăng ký tài khoản mới", description = "API để đăng ký tài khoản người dùng mới")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Đăng ký thành công",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ hoặc người dùng đã tồn tại")
    })
    @PostMapping("/register")
    public ResponseEntity<com.example.app.dto.ApiResponse<UserDTO>> register(
            @Parameter(description = "Thông tin đăng ký", required = true)
            @Valid @RequestBody UserCreateDTO registerRequest) {
        UserDTO userDTO = authService.register(registerRequest);
        return new ResponseEntity<>(
                com.example.app.dto.ApiResponse.success("Đăng ký thành công", userDTO),
                HttpStatus.CREATED);
    }

    /**
     * Lấy thông tin người dùng hiện tại
     */
    @Operation(summary = "Lấy thông tin người dùng hiện tại", description = "API để lấy thông tin người dùng đã đăng nhập")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy thông tin thành công",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserDTO.class))),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập")
    })
    @GetMapping("/me")
    public ResponseEntity<com.example.app.dto.ApiResponse<UserDTO>> getCurrentUser() {
        UserDTO userDTO = authService.getCurrentUser();
        if (userDTO != null) {
            return ResponseEntity.ok(com.example.app.dto.ApiResponse.success("User information retrieved successfully", userDTO));
        } else {
            // Tạo đối tượng ApiResponse<UserDTO> với data là null
            com.example.app.dto.ApiResponse<UserDTO> response = new com.example.app.dto.ApiResponse<>(false, "User not authenticated", null);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    /**
     * Đăng xuất
     */
    @Operation(summary = "Đăng xuất", description = "API để đăng xuất người dùng hiện tại")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Đăng xuất thành công")
    })
    @PostMapping("/logout")
    public ResponseEntity<com.example.app.dto.ApiResponse<?>> logout() {
        // Xóa Authentication khỏi SecurityContext
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(com.example.app.dto.ApiResponse.success("Đăng xuất thành công"));
    }

    /**
     * Kiểm tra token có hợp lệ không
     */
    @Operation(summary = "Kiểm tra token", description = "API để kiểm tra JWT token có hợp lệ không")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Kiểm tra thành công",
                    content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/validate-token")
    public ResponseEntity<com.example.app.dto.ApiResponse<Boolean>> validateToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isValid = authentication != null && authentication.isAuthenticated()
                && !authentication.getPrincipal().equals("anonymousUser");
        return ResponseEntity.ok(com.example.app.dto.ApiResponse.success("Token validation status", isValid));
    }
}
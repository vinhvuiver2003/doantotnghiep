package com.example.app.controller;
import com.example.app.dto.*;
import com.example.app.dto.ResponseWrapper;
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

    @PostMapping("/login")
    public ResponseEntity<ResponseWrapper<LoginResponse>> login(@Valid @RequestBody LoginRequest loginRequest) {
        LoginResponse loginResponse = authService.login(loginRequest);
        return ResponseEntity.ok(ResponseWrapper.success("Đăng nhập thành công", loginResponse));
    }


    @PostMapping("/register")
    public ResponseEntity<ResponseWrapper<UserDTO>> register(@Valid @RequestBody UserCreateDTO registerRequest) {
        UserDTO userDTO = authService.register(registerRequest);
        return new ResponseEntity<>(
                ResponseWrapper.success("Đăng ký thành công", userDTO),
                HttpStatus.CREATED);
    }


    @GetMapping("/me")
    public ResponseEntity<ResponseWrapper<UserDTO>> getCurrentUser() {
        UserDTO userDTO = authService.getCurrentUser();
        if (userDTO != null) {
            return ResponseEntity.ok(ResponseWrapper.success("User information retrieved successfully", userDTO));
        } else {
            ResponseWrapper<UserDTO> response = new ResponseWrapper<>(false, "User not authenticated", null);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }


    @PostMapping("/logout")
    public ResponseEntity<ResponseWrapper<?>> logout() {
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(ResponseWrapper.success("Đăng xuất thành công"));
    }


    @GetMapping("/validate-token")
    public ResponseEntity<ResponseWrapper<Boolean>> validateToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isValid = authentication != null && authentication.isAuthenticated()
                && !authentication.getPrincipal().equals("anonymousUser");
        return ResponseEntity.ok(ResponseWrapper.success("Token validation status", isValid));
    }
}
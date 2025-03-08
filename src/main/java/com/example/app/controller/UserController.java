package com.example.app.controller;
import com.example.app.dto.ApiResponse;
import com.example.app.dto.ChangePasswordRequest;
import com.example.app.dto.PagedResponse;
import com.example.app.dto.UserDTO;
import com.example.app.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Lấy danh sách tất cả người dùng (chỉ ADMIN)
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PagedResponse<UserDTO>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        PagedResponse<UserDTO> users = userService.getAllUsers(page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully", users));
    }

    /**
     * Lấy thông tin người dùng theo ID (ADMIN hoặc chính người dùng đó)
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isCurrentUser(#id)")
    public ResponseEntity<ApiResponse<UserDTO>> getUserById(@PathVariable Integer id) {
        UserDTO user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success("User retrieved successfully", user));
    }

    /**
     * Lấy thông tin người dùng hiện tại
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDTO>> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        UserDTO user = userService.getUserByUsername(username);
        return ResponseEntity.ok(ApiResponse.success("Current user retrieved successfully", user));
    }

    /**
     * Lấy danh sách người dùng theo vai trò (chỉ ADMIN)
     */
    @GetMapping("/role/{roleId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserDTO>>> getUsersByRole(@PathVariable Integer roleId) {
        List<UserDTO> users = userService.getUsersByRole(roleId);
        return ResponseEntity.ok(ApiResponse.success("Users by role retrieved successfully", users));
    }

    /**
     * Cập nhật thông tin người dùng (ADMIN hoặc chính người dùng đó)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isCurrentUser(#id)")
    public ResponseEntity<ApiResponse<UserDTO>> updateUser(
            @PathVariable Integer id,
            @Valid @RequestBody UserDTO userDTO) {

        UserDTO updatedUser = userService.updateUser(id, userDTO);
        return ResponseEntity.ok(ApiResponse.success("User updated successfully", updatedUser));
    }

    /**
     * Đổi mật khẩu (chỉ cho người dùng hiện tại)
     */
    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Boolean>> changePassword(
            @Valid @RequestBody ChangePasswordRequest changePasswordRequest) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        UserDTO user = userService.getUserByUsername(username);

        boolean changed = userService.changePassword(user.getId(), changePasswordRequest);

        return ResponseEntity.ok(ApiResponse.success("Password changed successfully", changed));
    }

    /**
     * Xóa người dùng (chỉ ADMIN)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<?>> deleteUser(@PathVariable Integer id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("User deleted successfully"));
    }
}
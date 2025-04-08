package com.example.app.controller;
import com.example.app.dto.ResponseWrapper;
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

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseWrapper<PagedResponse<UserDTO>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        PagedResponse<UserDTO> users = userService.getAllUsers(page, size, sortBy, sortDir);
        return ResponseEntity.ok(ResponseWrapper.success("Users retrieved successfully", users));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isCurrentUser(#id)")
    public ResponseEntity<ResponseWrapper<UserDTO>> getUserById(@PathVariable Integer id) {
        UserDTO user = userService.getUserById(id);
        return ResponseEntity.ok(ResponseWrapper.success("User retrieved successfully", user));
    }


    @GetMapping("/me")
    public ResponseEntity<ResponseWrapper<UserDTO>> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        UserDTO user = userService.getUserByUsername(username);
        return ResponseEntity.ok(ResponseWrapper.success("Current user retrieved successfully", user));
    }


    @GetMapping("/role/{roleId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseWrapper<List<UserDTO>>> getUsersByRole(@PathVariable Integer roleId) {
        List<UserDTO> users = userService.getUsersByRole(roleId);
        return ResponseEntity.ok(ResponseWrapper.success("Users by role retrieved successfully", users));
    }


    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isCurrentUser(#id)")
    public ResponseEntity<ResponseWrapper<UserDTO>> updateUser(
            @PathVariable Integer id,
            @Valid @RequestBody UserDTO userDTO) {

        UserDTO updatedUser = userService.updateUser(id, userDTO);
        return ResponseEntity.ok(ResponseWrapper.success("User updated successfully", updatedUser));
    }


    @PutMapping("/profile")
    public ResponseEntity<ResponseWrapper<UserDTO>> updateCurrentUserProfile(
            @Valid @RequestBody UserDTO userDTO) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        UserDTO currentUser = userService.getUserByUsername(username);
        
        UserDTO updatedUser = userService.updateUser(currentUser.getId(), userDTO);
        return ResponseEntity.ok(ResponseWrapper.success("Profile updated successfully", updatedUser));
    }

    @PostMapping("/change-password")
    public ResponseEntity<ResponseWrapper<Boolean>> changePassword(
            @Valid @RequestBody ChangePasswordRequest changePasswordRequest) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        UserDTO user = userService.getUserByUsername(username);

        boolean changed = userService.changePassword(user.getId(), changePasswordRequest);

        return ResponseEntity.ok(ResponseWrapper.success("Password changed successfully", changed));
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseWrapper<?>> deleteUser(@PathVariable Integer id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ResponseWrapper.success("User deleted successfully"));
    }
}
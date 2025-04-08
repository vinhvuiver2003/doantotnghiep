package com.example.app.controller;

import com.example.app.dto.ResponseWrapper;
import com.example.app.dto.RoleDTO;
import com.example.app.service.RoleService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@PreAuthorize("hasRole('ADMIN')")  // Chỉ ADMIN mới có quyền quản lý vai trò
public class RoleController {

    private final RoleService roleService;

    @Autowired
    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    /**
     * Lấy danh sách tất cả vai trò
     */
    @GetMapping
    public ResponseEntity<ResponseWrapper<List<RoleDTO>>> getAllRoles() {
        List<RoleDTO> roles = roleService.getAllRoles();
        return ResponseEntity.ok(ResponseWrapper.success("Roles retrieved successfully", roles));
    }

    /**
     * Lấy thông tin một vai trò theo ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ResponseWrapper<RoleDTO>> getRoleById(@PathVariable Integer id) {
        RoleDTO role = roleService.getRoleById(id);
        return ResponseEntity.ok(ResponseWrapper.success("Role retrieved successfully", role));
    }

    /**
     * Lấy thông tin một vai trò theo tên
     */
    @GetMapping("/name/{name}")
    public ResponseEntity<ResponseWrapper<RoleDTO>> getRoleByName(@PathVariable String name) {
        RoleDTO role = roleService.getRoleByName(name);
        return ResponseEntity.ok(ResponseWrapper.success("Role retrieved successfully", role));
    }

    /**
     * Tạo mới một vai trò
     */
    @PostMapping
    public ResponseEntity<ResponseWrapper<RoleDTO>> createRole(@Valid @RequestBody RoleDTO roleDTO) {
        RoleDTO createdRole = roleService.createRole(roleDTO);
        return new ResponseEntity<>(
                ResponseWrapper.success("Role created successfully", createdRole),
                HttpStatus.CREATED);
    }

    /**
     * Cập nhật thông tin một vai trò
     */
    @PutMapping("/{id}")
    public ResponseEntity<ResponseWrapper<RoleDTO>> updateRole(
            @PathVariable Integer id,
            @Valid @RequestBody RoleDTO roleDTO) {

        RoleDTO updatedRole = roleService.updateRole(id, roleDTO);
        return ResponseEntity.ok(ResponseWrapper.success("Role updated successfully", updatedRole));
    }

    /**
     * Xóa một vai trò
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseWrapper<?>> deleteRole(@PathVariable Integer id) {
        roleService.deleteRole(id);
        return ResponseEntity.ok(ResponseWrapper.success("Role deleted successfully"));
    }

    /**
     * Kiểm tra xem một vai trò có tồn tại theo tên
     */
    @GetMapping("/exists/{name}")
    public ResponseEntity<ResponseWrapper<Boolean>> checkRoleExists(@PathVariable String name) {
        boolean exists = roleService.existsByName(name);
        return ResponseEntity.ok(ResponseWrapper.success("Role existence checked", exists));
    }
}
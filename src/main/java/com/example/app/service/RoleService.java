package com.example.app.service;

import com.example.app.dto.RoleDTO;
import java.util.List;

public interface RoleService {
    List<RoleDTO> getAllRoles();

    RoleDTO getRoleById(Integer id);

    RoleDTO getRoleByName(String name);

    RoleDTO createRole(RoleDTO roleDTO);

    RoleDTO updateRole(Integer id, RoleDTO roleDTO);

    void deleteRole(Integer id);

    boolean existsByName(String name);
}

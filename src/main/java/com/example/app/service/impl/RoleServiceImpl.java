package com.example.app.service.impl;

import com.example.app.dto.RoleDTO;
import com.example.app.entity.Role;
import com.example.app.exception.ResourceNotFoundException;
import com.example.app.repository.RoleRepository;
import com.example.app.repository.UserRepository;
import com.example.app.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    @Autowired
    public RoleServiceImpl(RoleRepository roleRepository, UserRepository userRepository) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
    }

    @Override
    public List<RoleDTO> getAllRoles() {
        List<Role> roles = roleRepository.findAll();
        return roles.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public RoleDTO getRoleById(Integer id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + id));
        return convertToDTO(role);
    }

    @Override
    public RoleDTO getRoleByName(String name) {
        Role role = roleRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with name: " + name));
        return convertToDTO(role);
    }

    @Override
    @Transactional
    public RoleDTO createRole(RoleDTO roleDTO) {
        // Kiểm tra tên vai trò đã tồn tại chưa
        if (roleRepository.existsByName(roleDTO.getName())) {
            throw new IllegalArgumentException("Role already exists with name: " + roleDTO.getName());
        }

        Role role = new Role();
        role.setName(roleDTO.getName());

        Role savedRole = roleRepository.save(role);
        return convertToDTO(savedRole);
    }

    @Override
    @Transactional
    public RoleDTO updateRole(Integer id, RoleDTO roleDTO) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + id));

        if (!role.getName().equals(roleDTO.getName()) && roleRepository.existsByName(roleDTO.getName())) {
            throw new IllegalArgumentException("Role already exists with name: " + roleDTO.getName());
        }

        role.setName(roleDTO.getName());

        Role updatedRole = roleRepository.save(role);
        return convertToDTO(updatedRole);
    }

    @Override
    @Transactional
    public void deleteRole(Integer id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + id));

        long userCount = userRepository.findByRoleId(id).size();
        if (userCount > 0) {
            throw new IllegalArgumentException("Cannot delete role because it is assigned to " + userCount + " users");
        }

        roleRepository.deleteById(id);
    }

    @Override
    public boolean existsByName(String name) {
        return roleRepository.existsByName(name);
    }

    private RoleDTO convertToDTO(Role role) {
        RoleDTO dto = new RoleDTO();
        dto.setId(role.getId());
        dto.setName(role.getName());
        return dto;
    }
}
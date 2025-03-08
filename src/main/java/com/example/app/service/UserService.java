package com.example.app.service;

import com.example.app.dto.*;

import java.util.List;

public interface UserService {
    PagedResponse<UserDTO> getAllUsers(int page, int size, String sortBy, String sortDir);

    UserDTO getUserById(Integer id);

    UserDTO getUserByUsername(String username);

    UserDTO registerUser(UserCreateDTO userCreateDTO);

    UserDTO updateUser(Integer id, UserDTO userDTO);

    void deleteUser(Integer id);

    LoginResponse login(LoginRequest loginRequest);

    boolean changePassword(Integer userId, ChangePasswordRequest changePasswordRequest);

    List<UserDTO> getUsersByRole(Integer roleId);

    Long countUsers();
}
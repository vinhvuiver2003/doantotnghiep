package com.example.app.service;

// AuthService Interface


import com.example.app.dto.LoginRequest;
import com.example.app.dto.LoginResponse;
import com.example.app.dto.UserCreateDTO;
import com.example.app.dto.UserDTO;

public interface AuthService {

    LoginResponse login(LoginRequest loginRequest);


    UserDTO register(UserCreateDTO registerRequest);


    UserDTO getCurrentUser();


    boolean validateToken(String token);

    UserDTO getUserByToken(String token);


    void updateLastLogin(Integer userId);
}
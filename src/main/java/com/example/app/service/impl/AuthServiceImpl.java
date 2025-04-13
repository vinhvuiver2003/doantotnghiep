package com.example.app.service.impl;

import com.example.app.dto.LoginRequest;
import com.example.app.dto.LoginResponse;
import com.example.app.dto.UserCreateDTO;
import com.example.app.dto.UserDTO;
import com.example.app.entity.User;
import com.example.app.exception.ResourceNotFoundException;
import com.example.app.repository.UserRepository;
import com.example.app.security.CustomUserDetailsService;
import com.example.app.service.AuthService;
import com.example.app.service.EmailService;
import com.example.app.service.UserService;
import com.example.app.service.VerificationTokenService;
import com.example.app.util.EmailUtils;
import com.example.app.util.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;
    private final UserService userService;
    private final CustomUserDetailsService customUserDetailsService;
    private final EmailService emailService;
    private final EmailUtils emailUtils;
    private final VerificationTokenService verificationTokenService;

    @Value("${app.email.verification-expiry-hours}")
    private int verificationExpiryHours;

    @Autowired
    public AuthServiceImpl(
            AuthenticationManager authenticationManager,
            JwtUtils jwtUtils,
            UserRepository userRepository,
            UserService userService,
            CustomUserDetailsService customUserDetailsService,
            EmailService emailService,
            EmailUtils emailUtils,
            VerificationTokenService verificationTokenService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.userRepository = userRepository;
        this.userService = userService;
        this.customUserDetailsService = customUserDetailsService;
        this.emailService = emailService;
        this.emailUtils = emailUtils;
        this.verificationTokenService = verificationTokenService;
    }

    @Override
    @Transactional
    public LoginResponse login(LoginRequest loginRequest) {
        // Xác thực thông tin đăng nhập
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsernameOrEmail(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = jwtUtils.generateJwtToken(authentication);

        User user;
        if (loginRequest.getUsernameOrEmail().contains("@")) {
            user = userRepository.findByEmail(loginRequest.getUsernameOrEmail())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with emails: " + loginRequest.getUsernameOrEmail()));
        } else {
            user = userRepository.findByUsername(loginRequest.getUsernameOrEmail())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + loginRequest.getUsernameOrEmail()));
        }

        updateLastLogin(user.getId());

        UserDTO userDTO = userService.getUserById(user.getId());
        return new LoginResponse(jwt, "Bearer", userDTO);
    }

    @Override
    @Transactional
    public UserDTO register(UserCreateDTO registerRequest) {
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new IllegalArgumentException("Username already exists: " + registerRequest.getUsername());
        }

        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + registerRequest.getEmail());
        }

        if (registerRequest.getRole() == null) {
            registerRequest.setRole("USER");
        }

        UserDTO newUser = userService.registerUser(registerRequest);

        User user = userRepository.findById(newUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found after registration"));

        String verificationToken = verificationTokenService.createVerificationToken(user, verificationExpiryHours).getToken();
        String verificationLink = emailUtils.generateVerificationLink(verificationToken);

        emailService.sendVerificationEmail(
                registerRequest.getEmail(),
                registerRequest.getFirstName() + " " + registerRequest.getLastName(),
                verificationLink
        );

        return newUser;
    }

    @Override
    public UserDTO getCurrentUser() {
        // Lấy Authentication từ SecurityContext
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            try {
                return userService.getUserByUsername(username);
            } catch (ResourceNotFoundException e) {
                return null;
            }
        }

        return null;
    }

    @Override
    public boolean validateToken(String token) {
        return jwtUtils.validateToken(token);
    }

    @Override
    public UserDTO getUserByToken(String token) {
        if (!validateToken(token)) {
            return null;
        }

        String username = jwtUtils.getUsernameFromJWT(token);

        try {
            return userService.getUserByUsername(username);
        } catch (ResourceNotFoundException e) {
            return null;
        }
    }

    @Override
    public void updateLastLogin(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);
    }
}
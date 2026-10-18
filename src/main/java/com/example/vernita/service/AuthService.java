
package com.example.vernita.service;

import com.example.vernita.dto.RegisterRequest;
import com.example.vernita.dto.RegisterResponse;
import com.example.vernita.entity.User;
import com.example.vernita.entity.UserProfile;
import com.example.vernita.exception.EmailAlreadyExistsException;
import com.example.vernita.repository.UserProfileRepository;
import com.example.vernita.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.vernita.dto.LoginRequest;
import com.example.vernita.dto.LoginResponse;
import com.example.vernita.service.JwtService;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public RegisterResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(
                    "Email is already registered"
            );
        }
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .createdAt(LocalDateTime.now())
                .build();

        User savedUser = userRepository.save(user);

        UserProfile profile = UserProfile.builder()
                .user(savedUser)
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .build();

        userProfileRepository.save(profile);

        return RegisterResponse.builder()
                .id(savedUser.getId())
                .fullName(profile.getFullName())
                .email(savedUser.getEmail())
                .message("Account created successfully")
                .build();
    }


    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new IllegalArgumentException("Invalid email or password"));

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword())) {

            throw new IllegalArgumentException("Invalid email or password");
        }

        UserProfile profile = userProfileRepository.findByUserId(user.getId())
                .orElseThrow(() ->
                        new IllegalStateException("User profile not found"));


        String token = jwtService.generateToken(user);

        return LoginResponse.builder()
                .id(user.getId())
                .fullName(profile.getFullName())
                .email(user.getEmail())
                .message("Login successful")
                .accessToken(token)
                .tokenType("Bearer")
                .build();

    }

}

package com.skillsync.service;

import com.skillsync.dto.*;
import com.skillsync.exception.ResourceNotFoundException;
import com.skillsync.exception.UserAlreadyExistsException;
import com.skillsync.model.User;
import com.skillsync.repository.UserRepository;
import com.skillsync.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse registerUser(UserRegistrationRequest request) {
        // Check if user already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("User with email " + request.getEmail() + " already exists");
        }

        // Create new user
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .currentRole(request.getCurrentRole())
                .targetRole(request.getTargetRole())
                .skills(request.getSkills())
                .experienceLevel(request.getExperienceLevel())
                .goals(request.getGoals())
                .githubUsername(request.getGithubUsername())
                .isActive(true)
                .build();

        User savedUser = userRepository.save(user);

        // Generate JWT token
        String token = jwtTokenProvider.generateToken(savedUser.getEmail());

        return AuthResponse.builder()
                .token(token)
                .userId(savedUser.getUserId())
                .email(savedUser.getEmail())
                .name(savedUser.getName())
                .build();
    }

    public AuthResponse loginUser(LoginRequest request) {
        // Authenticate user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        // Generate JWT token
        String token = jwtTokenProvider.generateToken(authentication);

        // Get user details
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return AuthResponse.builder()
                .token(token)
                .userId(user.getUserId())
                .email(user.getEmail())
                .name(user.getName())
                .build();
    }

    public UserResponse getUserById(UUID userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        return mapToUserResponse(user);
    }

    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        return mapToUserResponse(user);
    }

    @Transactional
    public UserResponse updateUser(UUID userId, UserUpdateRequest request) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Update user fields
        if (request.getName() != null) {
            user.setName(request.getName());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getCurrentRole() != null) {
            user.setCurrentRole(request.getCurrentRole());
        }
        if (request.getTargetRole() != null) {
            user.setTargetRole(request.getTargetRole());
        }
        if (request.getSkills() != null) {
            user.setSkills(request.getSkills());
        }
        if (request.getExperienceLevel() != null) {
            user.setExperienceLevel(request.getExperienceLevel());
        }
        if (request.getGoals() != null) {
            user.setGoals(request.getGoals());
        }
        if (request.getGithubUsername() != null) {
            user.setGithubUsername(request.getGithubUsername());
        }

        User updatedUser = userRepository.save(user);
        return mapToUserResponse(updatedUser);
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .userId(user.getUserId())
                .name(user.getName())
                .email(user.getEmail())
                .currentRole(user.getCurrentRole())
                .targetRole(user.getTargetRole())
                .skills(user.getSkills())
                .experienceLevel(user.getExperienceLevel())
                .goals(user.getGoals())
                .githubUsername(user.getGithubUsername())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}

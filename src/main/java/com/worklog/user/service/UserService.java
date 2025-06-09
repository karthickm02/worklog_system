package com.worklog.user.service;

import com.worklog.user.dto.CreateUserRequest;
import com.worklog.user.dto.UserResponse;
import com.worklog.user.model.User;
import com.worklog.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setRole(request.getRole());
        user.setDepartmentId(request.getDepartmentId());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setJoinDate(request.getJoinDate());

        User savedUser = userRepository.save(user);
        return UserResponse.fromUser(savedUser);
    }

    public Page<UserResponse> getUsers(String status, String role, UUID departmentId, String search, Pageable pageable) {
        return userRepository.findAllWithFilters(status, role, departmentId, search, pageable)
                .map(UserResponse::fromUser);
    }

    public UserResponse getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return UserResponse.fromUser(user);
    }

    public UserResponse getCurrentUser(UUID userId) {
        return getUserById(userId);
    }
} 
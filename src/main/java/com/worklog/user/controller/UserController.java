package com.worklog.user.controller;

import com.worklog.common.ApiResponse;
import com.worklog.security.UserPrincipal;
import com.worklog.user.dto.CreateUserRequest;
import com.worklog.user.dto.UserResponse;
import com.worklog.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        UserResponse user = userService.getCurrentUser(userPrincipal.getId());
        return ResponseEntity.ok(new ApiResponse<>("SUCCESS", user));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getUsers(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) UUID departmentId,
            @RequestParam(required = false) String search,
            Pageable pageable) {
        Page<UserResponse> users = userService.getUsers(status, role, departmentId, search, pageable);
        return ResponseEntity.ok(new ApiResponse<>("SUCCESS", users));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserResponse user = userService.createUser(request);
        return ResponseEntity.ok(new ApiResponse<>("SUCCESS", user));
    }
} 
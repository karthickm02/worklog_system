package com.worklog.auth;

import com.worklog.auth.dto.LoginRequest;
import com.worklog.auth.dto.TokenResponse;
import com.worklog.common.ApiResponse;
import com.worklog.security.RateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final RateLimiter rateLimiter;

    public AuthController(AuthService authService, RateLimiter rateLimiter) {
        this.authService = authService;
        this.rateLimiter = rateLimiter;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(
            @Valid @RequestBody LoginRequest loginRequest,
            HttpServletRequest request) {
        String clientIp = request.getRemoteAddr();
        if (!rateLimiter.tryConsume(clientIp)) {
            return ResponseEntity.status(429)
                .body(new ApiResponse<>("ERROR", null, "Too many requests. Please try again later.", "RATE_LIMIT_EXCEEDED"));
        }

        TokenResponse tokenResponse = authService.login(loginRequest);
        return ResponseEntity.ok(new ApiResponse<>("SUCCESS", tokenResponse));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refresh(
            @RequestBody String refreshToken,
            HttpServletRequest request) {
        String clientIp = request.getRemoteAddr();
        if (!rateLimiter.tryConsume(clientIp)) {
            return ResponseEntity.status(429)
                .body(new ApiResponse<>("ERROR", null, "Too many requests. Please try again later.", "RATE_LIMIT_EXCEEDED"));
        }

        TokenResponse tokenResponse = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(new ApiResponse<>("SUCCESS", tokenResponse));
    }
} 
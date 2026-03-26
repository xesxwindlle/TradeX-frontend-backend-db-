package com.tradex.controller;

import com.tradex.dto.request.ConfirmPasswordResetRequest;
import com.tradex.dto.request.LoginRequest;
import com.tradex.dto.request.RequestPasswordResetRequest;
import com.tradex.dto.request.SendVerificationCodeRequest;
import com.tradex.dto.request.SignupRequest;
import com.tradex.dto.response.ApiResponse;
import com.tradex.dto.response.AuthResponse;
import com.tradex.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse data = authService.login(request);
        return ResponseEntity.ok(ApiResponse.ok(data));
    }

    @PostMapping("/signup/send-code")
    public ResponseEntity<ApiResponse<Void>> sendSignupCode(
            @Valid @RequestBody SendVerificationCodeRequest request) {
        authService.sendSignupVerificationCode(request);
        return ResponseEntity.ok(ApiResponse.msg("Verification code sent to " + request.getEmail()));
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Void>> signup(@Valid @RequestBody SignupRequest request) {
        authService.signup(request);
        return ResponseEntity.ok(ApiResponse.msg("Account created successfully."));
    }

    @PostMapping("/password/reset/request")
    public ResponseEntity<ApiResponse<Void>> requestPasswordReset(
            @Valid @RequestBody RequestPasswordResetRequest request) {
        authService.requestPasswordReset(request);
        return ResponseEntity.ok(ApiResponse.msg("Password reset code sent."));
    }

    @PostMapping("/password/reset/confirm")
    public ResponseEntity<ApiResponse<Void>> confirmPasswordReset(
            @Valid @RequestBody ConfirmPasswordResetRequest request) {
        authService.confirmPasswordReset(request);
        return ResponseEntity.ok(ApiResponse.msg("Password has been reset."));
    }
}

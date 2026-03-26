package com.tradex.controller;

import com.tradex.dto.request.AddFundRequest;
import com.tradex.dto.request.SendMobileCodeRequest;
import com.tradex.dto.request.UpdateMobileRequest;
import com.tradex.dto.request.UpdatePasswordRequest;
import com.tradex.dto.response.AccountInfoResponse;
import com.tradex.dto.response.ApiResponse;
import com.tradex.security.UserPrincipal;
import com.tradex.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/account")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<AccountInfoResponse>> getAccountInfo(
            @AuthenticationPrincipal UserPrincipal principal) {
        AccountInfoResponse data = accountService.getAccountInfo(principal.getAccountNumber());
        return ResponseEntity.ok(ApiResponse.ok(data));
    }

    @PostMapping("/funds")
    public ResponseEntity<ApiResponse<Double>> addFund(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody AddFundRequest request) {
        double newBalance = accountService.addFund(principal.getAccountNumber(), request.getAmount());
        return ResponseEntity.ok(ApiResponse.ok(newBalance));
    }

    @PatchMapping("/password")
    public ResponseEntity<ApiResponse<Void>> updatePassword(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody UpdatePasswordRequest request) {
        accountService.updatePassword(principal.getAccountNumber(), request);
        return ResponseEntity.ok(ApiResponse.msg("Password updated."));
    }

    @PostMapping("/mobile/send-code")
    public ResponseEntity<ApiResponse<Void>> sendMobileCode(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody SendMobileCodeRequest request) {
        accountService.sendMobileVerificationCode(principal.getAccountNumber(), request);
        return ResponseEntity.ok(ApiResponse.msg("Verification code sent."));
    }

    @PatchMapping("/mobile")
    public ResponseEntity<ApiResponse<Void>> updateMobile(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody UpdateMobileRequest request) {
        accountService.updateMobileNumber(principal.getAccountNumber(), request);
        return ResponseEntity.ok(ApiResponse.msg("Mobile number updated."));
    }

    @PostMapping("/reset")
    public ResponseEntity<ApiResponse<Void>> resetAccount(
            @AuthenticationPrincipal UserPrincipal principal) {
        accountService.resetAccount(principal.getAccountNumber());
        return ResponseEntity.ok(ApiResponse.msg("Account reset successfully."));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> deleteAccount(
            @AuthenticationPrincipal UserPrincipal principal) {
        accountService.deleteAccount(principal.getAccountNumber());
        return ResponseEntity.ok(ApiResponse.msg("Account deleted."));
    }
}

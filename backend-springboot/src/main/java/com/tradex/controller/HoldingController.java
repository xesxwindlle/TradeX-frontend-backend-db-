package com.tradex.controller;

import com.tradex.dto.response.ApiResponse;
import com.tradex.dto.response.HoldingResponse;
import com.tradex.dto.response.ProfitLossResponse;
import com.tradex.security.UserPrincipal;
import com.tradex.service.HoldingService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/holdings")
public class HoldingController {

    private final HoldingService holdingService;

    public HoldingController(HoldingService holdingService) {
        this.holdingService = holdingService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<HoldingResponse>>> getHoldings(
            @AuthenticationPrincipal UserPrincipal principal) {
        List<HoldingResponse> data = holdingService.getHoldings(principal.getAccountNumber());
        return ResponseEntity.ok(ApiResponse.ok(data));
    }

    @GetMapping("/pnl")
    public ResponseEntity<ApiResponse<ProfitLossResponse>> getProfitLoss(
            @AuthenticationPrincipal UserPrincipal principal) {
        ProfitLossResponse data = holdingService.getProfitLoss(principal.getAccountNumber());
        return ResponseEntity.ok(ApiResponse.ok(data));
    }
}

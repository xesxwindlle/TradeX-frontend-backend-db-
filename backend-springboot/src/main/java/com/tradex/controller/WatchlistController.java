package com.tradex.controller;

import com.tradex.dto.request.WatchRequest;
import com.tradex.dto.response.ApiResponse;
import com.tradex.dto.response.WatchlistItemResponse;
import com.tradex.security.UserPrincipal;
import com.tradex.service.WatchlistService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/watchlist")
public class WatchlistController {

    private final WatchlistService watchlistService;

    public WatchlistController(WatchlistService watchlistService) {
        this.watchlistService = watchlistService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<WatchlistItemResponse>>> getWatchlist(
            @AuthenticationPrincipal UserPrincipal principal) {
        List<WatchlistItemResponse> data = watchlistService.getWatchlist(principal.getAccountNumber());
        return ResponseEntity.ok(ApiResponse.ok(data));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> addToWatchlist(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody WatchRequest request) {
        watchlistService.addToWatchlist(principal.getAccountNumber(), request.getSymbol());
        return ResponseEntity.ok(ApiResponse.msg("Symbol added to watchlist."));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> removeFromWatchlist(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody WatchRequest request) {
        watchlistService.removeFromWatchlist(principal.getAccountNumber(), request.getSymbol());
        return ResponseEntity.ok(ApiResponse.msg("Symbol removed from watchlist."));
    }
}

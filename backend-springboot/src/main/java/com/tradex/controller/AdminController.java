package com.tradex.controller;

import com.tradex.dto.response.ApiResponse;
import com.tradex.dto.response.OrderResponse;
import com.tradex.dto.response.UserSummaryResponse;
import com.tradex.model.SymbolStats;
import com.tradex.service.AdminService;
import com.tradex.service.OrderService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final OrderService orderService;

    public AdminController(AdminService adminService, OrderService orderService) {
        this.adminService = adminService;
        this.orderService = orderService;
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserSummaryResponse>>> getAllUsers() {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getAllUsers()));
    }

    @PostMapping("/users/{accountNumber}/reset")
    public ResponseEntity<ApiResponse<Void>> resetUser(@PathVariable int accountNumber) {
        adminService.resetUser(accountNumber);
        return ResponseEntity.ok(ApiResponse.msg("User reset successfully."));
    }

    @DeleteMapping("/users/{accountNumber}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable int accountNumber) {
        adminService.deleteUser(accountNumber);
        return ResponseEntity.ok(ApiResponse.msg("User deleted."));
    }

    @GetMapping("/orders")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getAllOrders(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return ResponseEntity.ok(ApiResponse.ok(orderService.getAllOrders(from, to)));
    }

    @GetMapping("/instruments")
    public ResponseEntity<ApiResponse<List<Object>>> getAllInstruments(
            @RequestParam(required = false) Integer limit) {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getAllInstrumentsWithMarketData(limit)));
    }

    @GetMapping("/stats/most-traded")
    public ResponseEntity<ApiResponse<List<SymbolStats>>> getMostTraded(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getMostTraded(limit)));
    }

    @GetMapping("/stats/most-held")
    public ResponseEntity<ApiResponse<List<SymbolStats>>> getMostHeld(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getMostHeld(limit)));
    }

    @GetMapping("/stats/most-watched")
    public ResponseEntity<ApiResponse<List<SymbolStats>>> getMostWatched(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getMostWatched(limit)));
    }
}

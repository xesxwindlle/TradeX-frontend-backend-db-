package com.tradex.controller;

import com.tradex.dto.request.OrderRequest;
import com.tradex.dto.response.ApiResponse;
import com.tradex.dto.response.OrderResponse;
import com.tradex.messaging.producer.OrderProducer;
import com.tradex.security.UserPrincipal;
import com.tradex.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final OrderProducer orderProducer;

    public OrderController(OrderService orderService, OrderProducer orderProducer) {
        this.orderService = orderService;
        this.orderProducer = orderProducer;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getOrders(
            @AuthenticationPrincipal UserPrincipal principal) {
        List<OrderResponse> data = orderService.getOrdersForAccount(principal.getAccountNumber());
        return ResponseEntity.ok(ApiResponse.ok(data));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createOrder(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody OrderRequest request) {
        orderProducer.sendOrder(principal.getAccountNumber(), request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(ApiResponse.msg("Order received"));
    }
}

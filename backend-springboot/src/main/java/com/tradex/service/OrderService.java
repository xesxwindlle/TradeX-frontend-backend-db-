package com.tradex.service;

import com.tradex.dto.request.OrderRequest;
import com.tradex.dto.response.OrderResponse;
import com.tradex.enums.OrderAction;
import com.tradex.enums.OrderStatus;
import com.tradex.exception.BadRequestException;
import com.tradex.exception.ResourceNotFoundException;
import com.tradex.model.Account;
import com.tradex.model.Holding;
import com.tradex.model.Order;
import com.tradex.repository.AccountRepository;
import com.tradex.repository.HoldingRepository;
import com.tradex.repository.OrderRepository;
import com.tradex.util.IdGenerator;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final AccountRepository accountRepository;
    private final HoldingRepository holdingRepository;

    public OrderService(OrderRepository orderRepository,
                        AccountRepository accountRepository,
                        HoldingRepository holdingRepository) {
        this.orderRepository = orderRepository;
        this.accountRepository = accountRepository;
        this.holdingRepository = holdingRepository;
    }

    public String createOrder(int accountNumber, OrderRequest request) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        double orderTotal = request.getUnitPrice() * request.getQuantity();

        if (request.getAction() == OrderAction.Buy) {
            if (account.getCashBalance() < orderTotal) {
                throw new BadRequestException("Insufficient funds. Required: " + orderTotal
                        + ", Available: " + account.getCashBalance());
            }
            executeBuy(account, request, orderTotal);

        } else {
            executeSell(account, request, orderTotal);
        }

        Order order = new Order();
        order.setId(IdGenerator.generate(10));
        order.setAccountNumber(accountNumber);
        order.setSymbol(request.getSymbol());
        order.setUnitPrice(request.getUnitPrice());
        order.setQuantity(request.getQuantity());
        order.setAction(request.getAction());
        order.setCreatedTime(LocalDateTime.now());
        order.setStatus(OrderStatus.Filled);
        orderRepository.save(order);

        return order.getId();
    }

    private void executeBuy(Account account, OrderRequest request, double orderTotal) {
        double newCash = account.getCashBalance() - orderTotal;
        double newMarketValue = account.getMarketValue() + orderTotal;
        accountRepository.updateBalances(account.getAccountNumber(), newCash, newMarketValue);

        Optional<Holding> existing = holdingRepository.findByAccountNumberAndSymbol(
                account.getAccountNumber(), request.getSymbol());

        if (existing.isPresent()) {
            Holding h = existing.get();
            double newQty = h.getQuantity() + request.getQuantity();
            double newAvgPrice = ((h.getQuantity() * h.getUnitPrice()) +
                    (request.getQuantity() * request.getUnitPrice())) / newQty;
            holdingRepository.update(account.getAccountNumber(), request.getSymbol(), newAvgPrice, newQty);
        } else {
            holdingRepository.save(new Holding(account.getAccountNumber(),
                    request.getSymbol(), request.getUnitPrice(), request.getQuantity()));
        }
    }

    private void executeSell(Account account, OrderRequest request, double orderTotal) {
        Holding holding = holdingRepository
                .findByAccountNumberAndSymbol(account.getAccountNumber(), request.getSymbol())
                .orElseThrow(() -> new BadRequestException("No holding found for symbol: " + request.getSymbol()));

        if (request.getQuantity() > holding.getQuantity()) {
            throw new BadRequestException("Insufficient shares. Owned: " + holding.getQuantity()
                    + ", Requested: " + request.getQuantity());
        }

        double newCash = account.getCashBalance() + orderTotal;
        double newMarketValue = Math.max(0, account.getMarketValue() - orderTotal);
        accountRepository.updateBalances(account.getAccountNumber(), newCash, newMarketValue);

        double remainingQty = holding.getQuantity() - request.getQuantity();
        if (remainingQty == 0) {
            holdingRepository.delete(account.getAccountNumber(), request.getSymbol());
        } else {
            holdingRepository.update(account.getAccountNumber(), request.getSymbol(),
                    holding.getUnitPrice(), remainingQty);
        }
    }

    public List<OrderResponse> getOrdersForAccount(int accountNumber) {
        return orderRepository.findByAccountNumber(accountNumber)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<OrderResponse> getAllOrders(LocalDateTime from, LocalDateTime to) {
        List<Order> orders = (from != null && to != null)
                ? orderRepository.findAllByDateRange(from, to)
                : orderRepository.findAll();
        return orders.stream().map(this::toResponse).collect(Collectors.toList());
    }

    private OrderResponse toResponse(Order o) {
        OrderResponse r = new OrderResponse();
        r.setId(o.getId());
        r.setSymbol(o.getSymbol());
        r.setUnitPrice(o.getUnitPrice());
        r.setQuantity(o.getQuantity());
        r.setAction(o.getAction().name());
        r.setStatus(o.getStatus().name());
        r.setCreatedTime(o.getCreatedTime());
        return r;
    }
}

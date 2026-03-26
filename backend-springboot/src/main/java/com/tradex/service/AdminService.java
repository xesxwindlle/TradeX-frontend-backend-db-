package com.tradex.service;

import com.tradex.dto.response.UserSummaryResponse;
import com.tradex.model.Account;
import com.tradex.model.Order;
import com.tradex.model.SymbolStats;
import com.tradex.model.User;
import com.tradex.repository.AccountRepository;
import com.tradex.repository.HoldingRepository;
import com.tradex.repository.OrderRepository;
import com.tradex.repository.UserRepository;
import com.tradex.repository.WatchlistRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final OrderRepository orderRepository;
    private final HoldingRepository holdingRepository;
    private final WatchlistRepository watchlistRepository;

    public AdminService(UserRepository userRepository, AccountRepository accountRepository,
                        OrderRepository orderRepository, HoldingRepository holdingRepository,
                        WatchlistRepository watchlistRepository) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.orderRepository = orderRepository;
        this.holdingRepository = holdingRepository;
        this.watchlistRepository = watchlistRepository;
    }

    public List<UserSummaryResponse> getAllUsers() {
        List<User> users = userRepository.findAll();
        List<Account> accounts = accountRepository.findAll();

        Map<Integer, Account> accountMap = new java.util.HashMap<>();
        for (Account a : accounts) accountMap.put(a.getAccountNumber(), a);

        List<UserSummaryResponse> result = new ArrayList<>();
        for (User u : users) {
            Account a = accountMap.get(u.getAccountNumber());
            if (a == null) continue;

            UserSummaryResponse r = new UserSummaryResponse();
            r.setId(u.getId());
            r.setEmail(u.getEmail());
            r.setPhoneNumber(u.getPhoneNumber());
            r.setFirstName(u.getFirstName());
            r.setMiddleName(u.getMiddleName());
            r.setLastName(u.getLastName());
            r.setBirthDate(u.getBirthDate());
            r.setReasonForSignup(u.getReasonForSignup());
            r.setAccountNumber(a.getAccountNumber());
            r.setCreatedDate(a.getCreatedDate());
            r.setCashBalance(a.getCashBalance());
            r.setMarketValue(a.getMarketValue());
            r.setRole(a.getRole().name());
            r.setStatus(a.getStatus().name());
            result.add(r);
        }
        return result;
    }

    public List<Order> getAllOrders(LocalDateTime from, LocalDateTime to) {
        return (from != null && to != null)
                ? orderRepository.findAllByDateRange(from, to)
                : orderRepository.findAll();
    }

    public void resetUser(int accountNumber) {
        watchlistRepository.deleteByAccountNumber(accountNumber);
        holdingRepository.deleteByAccountNumber(accountNumber);
        orderRepository.deleteByAccountNumber(accountNumber);
        accountRepository.resetCashBalance(accountNumber);
        accountRepository.resetMarketValue(accountNumber);
    }

    public void deleteUser(int accountNumber) {
        watchlistRepository.deleteByAccountNumber(accountNumber);
        holdingRepository.deleteByAccountNumber(accountNumber);
        orderRepository.deleteByAccountNumber(accountNumber);
        userRepository.delete(accountNumber);
        accountRepository.delete(accountNumber);
    }

    public List<Object> getAllInstrumentsWithMarketData(Integer limit) {
        return java.util.Collections.emptyList();
    }

    public List<SymbolStats> getMostTraded(int limit) {
        return orderRepository.findMostTraded(limit);
    }

    public List<SymbolStats> getMostHeld(int limit) {
        return holdingRepository.findMostHeld(limit);
    }

    public List<SymbolStats> getMostWatched(int limit) {
        return watchlistRepository.findMostWatched(limit);
    }
}

package com.tradex.service;

import com.tradex.dto.request.SendMobileCodeRequest;
import com.tradex.dto.request.UpdateMobileRequest;
import com.tradex.dto.request.UpdatePasswordRequest;
import com.tradex.dto.response.AccountInfoResponse;
import com.tradex.exception.BadRequestException;
import com.tradex.exception.ResourceNotFoundException;
import com.tradex.model.Account;
import com.tradex.model.User;
import com.tradex.repository.AccountRepository;
import com.tradex.repository.HoldingRepository;
import com.tradex.repository.OrderRepository;
import com.tradex.repository.UserRepository;
import com.tradex.repository.WatchlistRepository;
import com.tradex.util.EmailService;
import com.tradex.util.IdGenerator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final HoldingRepository holdingRepository;
    private final WatchlistRepository watchlistRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public AccountService(AccountRepository accountRepository, UserRepository userRepository,
                          OrderRepository orderRepository, HoldingRepository holdingRepository,
                          WatchlistRepository watchlistRepository,
                          PasswordEncoder passwordEncoder, EmailService emailService) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.holdingRepository = holdingRepository;
        this.watchlistRepository = watchlistRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    public AccountInfoResponse getAccountInfo(int accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
        User user = userRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        AccountInfoResponse response = new AccountInfoResponse();
        response.setAccountNumber(account.getAccountNumber());
        response.setEmail(account.getEmail());
        response.setCashBalance(account.getCashBalance());
        response.setMarketValue(account.getMarketValue());
        response.setRole(account.getRole().name());
        response.setStatus(account.getStatus().name());
        response.setCreatedDate(account.getCreatedDate());
        response.setFirstName(user.getFirstName());
        response.setMiddleName(user.getMiddleName());
        response.setLastName(user.getLastName());
        response.setPhoneNumber(user.getPhoneNumber());
        response.setBirthDate(user.getBirthDate());
        response.setReasonForSignup(user.getReasonForSignup());
        return response;
    }

    public double addFund(int accountNumber, double amount) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
        double newBalance = account.getCashBalance() + amount;
        accountRepository.updateCashBalance(accountNumber, newBalance);
        return newBalance;
    }

    public void updatePassword(int accountNumber, UpdatePasswordRequest request) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), account.getLoginPassword())) {
            throw new BadRequestException("Current password is incorrect.");
        }

        accountRepository.updatePassword(account.getEmail(),
                passwordEncoder.encode(request.getNewPassword()));
    }

    public void sendMobileVerificationCode(int accountNumber, SendMobileCodeRequest request) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        String code = IdGenerator.generateNumeric(6);
        accountRepository.saveVerificationCode(account.getEmail(), code);
        emailService.sendMobileVerificationEmail(account.getEmail(), code, request.getNewMobile());
    }

    public void updateMobileNumber(int accountNumber, UpdateMobileRequest request) {
        User user = userRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!request.getCurrentMobile().equals(user.getPhoneNumber())) {
            throw new BadRequestException("Current mobile number is incorrect.");
        }

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        String storedCode = account.getVerificationCode();
        if (storedCode == null || !storedCode.equals(request.getVerificationCode())) {
            throw new BadRequestException("Invalid or expired verification code.");
        }

        userRepository.updatePhoneNumber(accountNumber, request.getNewMobile());
        accountRepository.clearVerificationCode(account.getEmail());
    }

    public void resetAccount(int accountNumber) {
        watchlistRepository.deleteByAccountNumber(accountNumber);
        holdingRepository.deleteByAccountNumber(accountNumber);
        orderRepository.deleteByAccountNumber(accountNumber);
        accountRepository.resetCashBalance(accountNumber);
        accountRepository.resetMarketValue(accountNumber);
    }

    public void deleteAccount(int accountNumber) {
        watchlistRepository.deleteByAccountNumber(accountNumber);
        holdingRepository.deleteByAccountNumber(accountNumber);
        orderRepository.deleteByAccountNumber(accountNumber);
        userRepository.delete(accountNumber);
        accountRepository.delete(accountNumber);
    }
}

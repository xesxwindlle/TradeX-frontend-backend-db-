package com.tradex.service;

import com.tradex.dto.request.*;
import com.tradex.dto.response.AuthResponse;
import com.tradex.enums.AccountStatus;
import com.tradex.enums.Role;
import com.tradex.exception.BadRequestException;
import com.tradex.model.Account;
import com.tradex.model.User;
import com.tradex.repository.AccountRepository;
import com.tradex.repository.UserRepository;
import com.tradex.security.JwtUtil;
import com.tradex.util.EmailService;
import com.tradex.util.IdGenerator;
import com.tradex.util.VerificationCodeStore;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class AuthService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final VerificationCodeStore codeStore;

    public AuthService(AccountRepository accountRepository, UserRepository userRepository,
                       JwtUtil jwtUtil, PasswordEncoder passwordEncoder,
                       EmailService emailService, VerificationCodeStore codeStore) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.codeStore = codeStore;
    }

    public AuthResponse login(LoginRequest request) {
        Account account = accountRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("Incorrect email or password."));

        if (!passwordEncoder.matches(request.getPassword(), account.getLoginPassword())) {
            throw new BadRequestException("Incorrect email or password.");
        }

        if (account.getStatus() == AccountStatus.Closed) {
            throw new BadRequestException("This account has been closed.");
        }

        String token = jwtUtil.generateToken(account.getEmail(), account.getAccountNumber());
        return new AuthResponse(token, account.getEmail(), account.getAccountNumber(),
                account.getRole().name(), account.getStatus().name());
    }

    public void sendSignupVerificationCode(SendVerificationCodeRequest request) {
        if (accountRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("An account with this email already exists.");
        }
        String code = IdGenerator.generateNumeric(6);
        codeStore.store(request.getEmail(), code);
        emailService.sendSignupVerificationEmail(request.getEmail(), code);
    }

    public void signup(SignupRequest request) {
        if (accountRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("An account with this email already exists.");
        }

        String storedCode = codeStore.retrieve(request.getEmail());
        if (storedCode == null || !storedCode.equals(request.getVerificationCode())) {
            throw new BadRequestException("Invalid or expired verification code.");
        }

        int accountNumber = accountRepository.getNextAccountNumber();
        int userId = userRepository.getNextId();

        Account account = new Account();
        account.setAccountNumber(accountNumber);
        account.setEmail(request.getEmail());
        account.setLoginPassword(passwordEncoder.encode(request.getPassword()));
        account.setCreatedDate(LocalDate.now());
        account.setCashBalance(0.0);
        account.setMarketValue(0.0);
        account.setRole(Role.Regular);
        account.setStatus(AccountStatus.Active);
        accountRepository.save(account);

        User user = new User();
        user.setId(userId);
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setAccountNumber(accountNumber);
        user.setFirstName(request.getFirstName());
        user.setMiddleName(request.getMiddleName());
        user.setLastName(request.getLastName());
        user.setReasonForSignup(request.getReasonForSignup());

        if (request.getBirthDate() != null && !request.getBirthDate().isBlank()) {
            user.setBirthDate(LocalDate.parse(request.getBirthDate(),
                    DateTimeFormatter.ofPattern("MM/dd/yyyy")));
        }

        userRepository.save(user);
    }

    public void requestPasswordReset(RequestPasswordResetRequest request) {
        if (!accountRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("No account found with that email.");
        }
        String code = IdGenerator.generateNumeric(6);
        accountRepository.saveVerificationCode(request.getEmail(), code);
        emailService.sendPasswordResetEmail(request.getEmail(), code);
    }

    public void confirmPasswordReset(ConfirmPasswordResetRequest request) {
        Account account = accountRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("No account found with that email."));

        String storedCode = account.getVerificationCode();
        if (storedCode == null || !storedCode.equals(request.getCode())) {
            throw new BadRequestException("Invalid or expired verification code.");
        }

        accountRepository.updatePassword(request.getEmail(),
                passwordEncoder.encode(request.getNewPassword()));
        accountRepository.clearVerificationCode(request.getEmail());
    }
}

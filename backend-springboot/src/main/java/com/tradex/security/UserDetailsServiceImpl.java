package com.tradex.security;

import com.tradex.model.Account;
import com.tradex.repository.AccountRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final AccountRepository accountRepository;

    public UserDetailsServiceImpl(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Account not found: " + email));

        String authority = "ROLE_" + account.getRole().name().toUpperCase();

        return new UserPrincipal(
                account.getAccountNumber(),
                account.getEmail(),
                account.getLoginPassword(),
                account.getStatus(),
                List.of(new SimpleGrantedAuthority(authority))
        );
    }
}

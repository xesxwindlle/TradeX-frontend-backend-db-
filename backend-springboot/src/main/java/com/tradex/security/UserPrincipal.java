package com.tradex.security;

import com.tradex.enums.AccountStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

public class UserPrincipal implements UserDetails {

    private final int accountNumber;
    private final String email;
    private final String password;
    private final AccountStatus status;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(int accountNumber, String email, String password,
                         AccountStatus status,
                         Collection<? extends GrantedAuthority> authorities) {
        this.accountNumber = accountNumber;
        this.email = email;
        this.password = password;
        this.status = status;
        this.authorities = authorities;
    }

    public int getAccountNumber() { return accountNumber; }

    @Override public String getUsername() { return email; }
    @Override public String getPassword() { return password; }
    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }

    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() {
        return status != AccountStatus.Frozen && status != AccountStatus.Closed;
    }

    @Override
    public boolean isEnabled() {
        return status != AccountStatus.Closed;
    }
}

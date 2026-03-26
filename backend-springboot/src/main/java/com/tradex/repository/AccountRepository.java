package com.tradex.repository;

import com.tradex.enums.AccountStatus;
import com.tradex.enums.Role;
import com.tradex.model.Account;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class AccountRepository {

    private final JdbcTemplate jdbc;

    public AccountRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<Account> mapper = (rs, row) -> {
        Account a = new Account();
        a.setAccountNumber(rs.getInt("account_number"));
        a.setEmail(rs.getString("email"));
        a.setLoginPassword(rs.getString("login_password"));
        a.setCashBalance(rs.getDouble("cash_balance"));
        a.setMarketValue(rs.getDouble("market_value"));
        a.setLinkedAccountInstitutionName(rs.getString("linked_account_institution_name"));
        int linked = rs.getInt("linked_account_number");
        a.setLinkedAccountNumber(rs.wasNull() ? null : linked);
        a.setRole(Role.valueOf(rs.getString("role")));
        a.setStatus(AccountStatus.valueOf(rs.getString("status")));
        a.setVerificationCode(rs.getString("verification_code"));
        java.sql.Date d = rs.getDate("created_date");
        if (d != null) a.setCreatedDate(d.toLocalDate());
        return a;
    };

    public void save(Account account) {
        jdbc.update(
            "INSERT INTO account (account_number, login_password, created_date, cash_balance, market_value, " +
            "linked_account_institution_name, linked_account_number, email, role, status) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
            account.getAccountNumber(), account.getLoginPassword(),
            account.getCreatedDate(), account.getCashBalance(), account.getMarketValue(),
            account.getLinkedAccountInstitutionName(), account.getLinkedAccountNumber(),
            account.getEmail(), account.getRole().name(), account.getStatus().name()
        );
    }

    public Optional<Account> findByEmail(String email) {
        List<Account> results = jdbc.query(
            "SELECT * FROM account WHERE email = ?", mapper, email);
        return results.stream().findFirst();
    }

    public Optional<Account> findByAccountNumber(int accountNumber) {
        List<Account> results = jdbc.query(
            "SELECT * FROM account WHERE account_number = ?", mapper, accountNumber);
        return results.stream().findFirst();
    }

    public List<Account> findAll() {
        return jdbc.query("SELECT * FROM account", mapper);
    }

    public boolean existsByEmail(String email) {
        Integer count = jdbc.queryForObject(
            "SELECT COUNT(*) FROM account WHERE email = ?", Integer.class, email);
        return count != null && count > 0;
    }

    public int updateCashBalance(int accountNumber, double cashBalance) {
        return jdbc.update(
            "UPDATE account SET cash_balance = ? WHERE account_number = ?",
            cashBalance, accountNumber);
    }

    public int updateBalances(int accountNumber, double cashBalance, double marketValue) {
        return jdbc.update(
            "UPDATE account SET cash_balance = ?, market_value = ? WHERE account_number = ?",
            cashBalance, marketValue, accountNumber);
    }

    public int updateMarketValue(int accountNumber, double marketValue) {
        return jdbc.update(
            "UPDATE account SET market_value = ? WHERE account_number = ?",
            marketValue, accountNumber);
    }

    public int updatePassword(String email, String newPassword) {
        return jdbc.update(
            "UPDATE account SET login_password = ? WHERE email = ?",
            newPassword, email);
    }

    public int saveVerificationCode(String email, String code) {
        return jdbc.update(
            "UPDATE account SET verification_code = ? WHERE email = ?",
            code, email);
    }

    public int clearVerificationCode(String email) {
        return jdbc.update(
            "UPDATE account SET verification_code = NULL WHERE email = ?", email);
    }

    public int resetCashBalance(int accountNumber) {
        return jdbc.update(
            "UPDATE account SET cash_balance = 0 WHERE account_number = ?", accountNumber);
    }

    public int resetMarketValue(int accountNumber) {
        return jdbc.update(
            "UPDATE account SET market_value = 0 WHERE account_number = ?", accountNumber);
    }

    public int delete(int accountNumber) {
        return jdbc.update(
            "DELETE FROM account WHERE account_number = ?", accountNumber);
    }

    public int getNextAccountNumber() {
        Integer min = jdbc.queryForObject(
            "SELECT MIN(account_number) FROM account", Integer.class);
        return (min == null) ? Integer.MAX_VALUE : min - 1;
    }
}

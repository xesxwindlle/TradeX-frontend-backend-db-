package com.tradex.repository;

import com.tradex.model.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class UserRepository {

    private final JdbcTemplate jdbc;

    public UserRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<User> mapper = (rs, row) -> {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setEmail(rs.getString("email"));
        u.setPhoneNumber(rs.getString("phone_number"));
        u.setAccountNumber(rs.getInt("account_number"));
        u.setFirstName(rs.getString("first_name"));
        u.setMiddleName(rs.getString("middle_name"));
        u.setLastName(rs.getString("last_name"));
        u.setReasonForSignup(rs.getString("reason_for_signup"));
        java.sql.Date d = rs.getDate("birth_date");
        if (d != null) u.setBirthDate(d.toLocalDate());
        return u;
    };

    public void save(User user) {
        jdbc.update(
            "INSERT INTO `user` (id, email, phone_number, account_number, first_name, middle_name, last_name, birth_date, reason_for_signup) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
            user.getId(), user.getEmail(), user.getPhoneNumber(), user.getAccountNumber(),
            user.getFirstName(), user.getMiddleName(), user.getLastName(),
            user.getBirthDate(), user.getReasonForSignup()
        );
    }

    public Optional<User> findByAccountNumber(int accountNumber) {
        List<User> results = jdbc.query(
            "SELECT * FROM `user` WHERE account_number = ?", mapper, accountNumber);
        return results.stream().findFirst();
    }

    public List<User> findAll() {
        return jdbc.query("SELECT * FROM `user` ORDER BY id ASC", mapper);
    }

    public int updatePhoneNumber(int accountNumber, String phoneNumber) {
        return jdbc.update(
            "UPDATE `user` SET phone_number = ? WHERE account_number = ?",
            phoneNumber, accountNumber);
    }

    public int delete(int accountNumber) {
        return jdbc.update(
            "DELETE FROM `user` WHERE account_number = ?", accountNumber);
    }

    public int getNextId() {
        Integer max = jdbc.queryForObject(
            "SELECT MAX(id) FROM `user`", Integer.class);
        return (max == null) ? 1 : max + 1;
    }
}

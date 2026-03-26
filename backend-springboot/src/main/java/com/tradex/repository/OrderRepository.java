package com.tradex.repository;

import com.tradex.enums.OrderAction;
import com.tradex.enums.OrderStatus;
import com.tradex.model.Order;
import com.tradex.model.SymbolStats;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class OrderRepository {

    private final JdbcTemplate jdbc;

    public OrderRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<Order> mapper = (rs, row) -> {
        Order o = new Order();
        o.setId(rs.getString("id"));
        o.setAccountNumber(rs.getInt("account_number"));
        o.setSymbol(rs.getString("symbol"));
        o.setUnitPrice(rs.getDouble("unit_price"));
        o.setQuantity(rs.getDouble("quantity"));
        o.setAction(OrderAction.valueOf(rs.getString("action")));
        o.setStatus(OrderStatus.valueOf(rs.getString("status")));
        java.sql.Timestamp ts = rs.getTimestamp("created_time");
        if (ts != null) o.setCreatedTime(ts.toLocalDateTime());
        return o;
    };

    public void save(Order order) {
        jdbc.update(
            "INSERT INTO `order` (id, account_number, symbol, unit_price, quantity, action, created_time, status) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
            order.getId(), order.getAccountNumber(), order.getSymbol(),
            order.getUnitPrice(), order.getQuantity(), order.getAction().name(),
            order.getCreatedTime(), order.getStatus().name()
        );
    }

    public List<Order> findByAccountNumber(int accountNumber) {
        return jdbc.query(
            "SELECT * FROM `order` WHERE account_number = ? ORDER BY created_time DESC",
            mapper, accountNumber);
    }

    public List<Order> findAll() {
        return jdbc.query(
            "SELECT * FROM `order` ORDER BY created_time DESC", mapper);
    }

    public List<Order> findAllByDateRange(LocalDateTime from, LocalDateTime to) {
        return jdbc.query(
            "SELECT * FROM `order` WHERE created_time BETWEEN ? AND ? ORDER BY created_time DESC",
            mapper, from, to);
    }

    public List<SymbolStats> findMostTraded(int limit) {
        return jdbc.query(
            "SELECT symbol, SUM(quantity) as trade_count FROM `order` GROUP BY symbol ORDER BY trade_count DESC LIMIT ?",
            (rs, row) -> new SymbolStats(rs.getString("symbol"), rs.getInt("trade_count")),
            limit);
    }

    public int deleteByAccountNumber(int accountNumber) {
        return jdbc.update("DELETE FROM `order` WHERE account_number = ?", accountNumber);
    }
}

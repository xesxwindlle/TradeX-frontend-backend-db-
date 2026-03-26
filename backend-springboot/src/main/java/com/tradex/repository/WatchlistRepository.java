package com.tradex.repository;

import com.tradex.model.SymbolStats;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class WatchlistRepository {

    private final JdbcTemplate jdbc;

    public WatchlistRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<String> findAllDistinctSymbols() {
        return jdbc.query(
            "SELECT DISTINCT symbol FROM watching",
            (rs, row) -> rs.getString("symbol"));
    }

    public List<Integer> findAccountNumbersBySymbol(String symbol) {
        return jdbc.query(
            "SELECT account_number FROM watching WHERE symbol = ?",
            (rs, row) -> rs.getInt("account_number"), symbol);
    }

    public boolean exists(int accountNumber, String symbol) {
        Integer count = jdbc.queryForObject(
            "SELECT COUNT(*) FROM watching WHERE account_number = ? AND symbol = ?",
            Integer.class, accountNumber, symbol);
        return count != null && count > 0;
    }

    public void save(int accountNumber, String symbol) {
        jdbc.update(
            "INSERT INTO watching (account_number, symbol) VALUES (?, ?)",
            accountNumber, symbol);
    }

    public int delete(int accountNumber, String symbol) {
        return jdbc.update(
            "DELETE FROM watching WHERE account_number = ? AND symbol = ?",
            accountNumber, symbol);
    }

    public int deleteByAccountNumber(int accountNumber) {
        return jdbc.update(
            "DELETE FROM watching WHERE account_number = ?", accountNumber);
    }

    public List<String> findSymbolsByAccountNumber(int accountNumber) {
        return jdbc.query(
            "SELECT symbol FROM watching WHERE account_number = ?",
            (rs, row) -> rs.getString("symbol"),
            accountNumber);
    }

    public List<SymbolStats> findMostWatched(int limit) {
        return jdbc.query(
            "SELECT symbol, COUNT(*) as watch_count FROM watching " +
            "GROUP BY symbol ORDER BY watch_count DESC LIMIT ?",
            (rs, row) -> new SymbolStats(rs.getString("symbol"), rs.getInt("watch_count")),
            limit);
    }
}

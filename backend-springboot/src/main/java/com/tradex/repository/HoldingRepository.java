package com.tradex.repository;

import com.tradex.model.Holding;
import com.tradex.model.SymbolStats;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class HoldingRepository {

    private final JdbcTemplate jdbc;

    public HoldingRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<Holding> mapper = (rs, row) -> {
        Holding h = new Holding();
        h.setAccountNumber(rs.getInt("account_number"));
        h.setSymbol(rs.getString("symbol"));
        h.setUnitPrice(rs.getDouble("unit_price"));
        h.setQuantity(rs.getDouble("quantity"));
        return h;
    };

    public Optional<Holding> findByAccountNumberAndSymbol(int accountNumber, String symbol) {
        List<Holding> results = jdbc.query(
            "SELECT * FROM holding WHERE account_number = ? AND symbol = ?",
            mapper, accountNumber, symbol);
        return results.stream().findFirst();
    }

    public List<Holding> findByAccountNumber(int accountNumber) {
        return jdbc.query(
            "SELECT * FROM holding WHERE account_number = ?",
            mapper, accountNumber);
    }

    public void save(Holding holding) {
        jdbc.update(
            "INSERT INTO holding (account_number, symbol, unit_price, quantity) VALUES (?, ?, ?, ?)",
            holding.getAccountNumber(), holding.getSymbol(), holding.getUnitPrice(), holding.getQuantity());
    }

    public int update(int accountNumber, String symbol, double unitPrice, double quantity) {
        return jdbc.update(
            "UPDATE holding SET unit_price = ?, quantity = ? WHERE account_number = ? AND symbol = ?",
            unitPrice, quantity, accountNumber, symbol);
    }

    public int delete(int accountNumber, String symbol) {
        return jdbc.update(
            "DELETE FROM holding WHERE account_number = ? AND symbol = ?",
            accountNumber, symbol);
    }

    public int deleteByAccountNumber(int accountNumber) {
        return jdbc.update(
            "DELETE FROM holding WHERE account_number = ?", accountNumber);
    }

    public List<SymbolStats> findMostHeld(int limit) {
        return jdbc.query(
            "SELECT symbol, COUNT(*) as holder_count FROM holding " +
            "GROUP BY symbol ORDER BY holder_count DESC LIMIT ?",
            (rs, row) -> new SymbolStats(rs.getString("symbol"), rs.getInt("holder_count")),
            limit);
    }

    public List<String> findAllDistinctSymbols() {
        return jdbc.query("SELECT DISTINCT symbol FROM holding",
        (rs, row) -> rs.getString("symbol"));
    }

    public List<Integer> findAccountNumbersBySymbol(String symbol) {
        return jdbc.query(
            "SELECT account_number FROM holding WHERE symbol = ?",
            (rs, row) -> rs.getInt("account_number"), symbol);
    }

}

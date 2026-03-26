package com.tradex.service;

import com.tradex.dto.response.WatchlistItemResponse;
import com.tradex.exception.BadRequestException;
import com.tradex.repository.WatchlistRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class WatchlistService {

    private final WatchlistRepository watchlistRepository;

    public WatchlistService(WatchlistRepository watchlistRepository) {
        this.watchlistRepository = watchlistRepository;
    }

    public List<WatchlistItemResponse> getWatchlist(int accountNumber) {
        return watchlistRepository.findSymbolsByAccountNumber(accountNumber)
                .stream()
                .map(symbol -> new WatchlistItemResponse(symbol, symbol, 0, 0))
                .collect(Collectors.toList());
    }

    public void addToWatchlist(int accountNumber, String symbol) {
        if (watchlistRepository.exists(accountNumber, symbol)) {
            throw new BadRequestException("Symbol already in watchlist.");
        }
        watchlistRepository.save(accountNumber, symbol);
    }

    public void removeFromWatchlist(int accountNumber, String symbol) {
        if (!watchlistRepository.exists(accountNumber, symbol)) {
            throw new BadRequestException("Symbol not found in watchlist.");
        }
        watchlistRepository.delete(accountNumber, symbol);
    }
}

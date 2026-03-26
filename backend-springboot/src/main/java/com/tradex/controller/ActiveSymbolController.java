package com.tradex.controller;

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tradex.repository.HoldingRepository;
import com.tradex.repository.WatchlistRepository;

@RestController
@RequestMapping("/api")
public class ActiveSymbolController {

    @Autowired
    private HoldingRepository hldRepo; 

    @Autowired
    private WatchlistRepository wtchRepo; 

    @GetMapping("/active-symbols")
    public Set<String> getActiveSymbols() {
        Set<String> symbols = new HashSet<>();
        symbols.addAll(hldRepo.findAllDistinctSymbols());
        symbols.addAll(wtchRepo.findAllDistinctSymbols());
        return symbols;
    }

    @GetMapping("/symbol-watchers")
    public Set<Integer> getSymbolWatchers(@RequestParam("symbol") String symbol) {
        Set<Integer> accounts = new HashSet<>();
        accounts.addAll(wtchRepo.findAccountNumbersBySymbol(symbol));
        accounts.addAll(hldRepo.findAccountNumbersBySymbol(symbol));
        return accounts;
    }
}

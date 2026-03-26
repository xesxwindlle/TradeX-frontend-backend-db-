package com.tradex.service;

import com.tradex.dto.response.HoldingResponse;
import com.tradex.dto.response.ProfitLossResponse;
import com.tradex.model.Holding;
import com.tradex.repository.HoldingRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class HoldingService {

    private final HoldingRepository holdingRepository;

    public HoldingService(HoldingRepository holdingRepository) {
        this.holdingRepository = holdingRepository;
    }

    public List<HoldingResponse> getHoldings(int accountNumber) {
        return holdingRepository.findByAccountNumber(accountNumber)
                .stream()
                .map(h -> new HoldingResponse(h.getSymbol(), h.getUnitPrice(), h.getQuantity(), h.getUnitPrice()))
                .collect(Collectors.toList());
    }

    public ProfitLossResponse getProfitLoss(int accountNumber) {
        List<Holding> holdings = holdingRepository.findByAccountNumber(accountNumber);

        double costBasis = holdings.stream()
                .mapToDouble(h -> h.getUnitPrice() * h.getQuantity())
                .sum();

        return new ProfitLossResponse(0, costBasis, costBasis, 0);
    }
}

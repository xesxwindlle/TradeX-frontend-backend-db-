package com.tradex.messaging.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tradex.dto.request.OrderRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class OrderProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderProducer.class);
    private static final String TOPIC = "order-events";

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    public void sendOrder(int accountNumber, OrderRequest request) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("accountNumber", accountNumber);
            message.put("symbol", request.getSymbol());
            message.put("unitPrice", request.getUnitPrice());
            message.put("quantity", request.getQuantity());
            message.put("action", request.getAction().name());

            String payload = objectMapper.writeValueAsString(message);
            kafkaTemplate.send(TOPIC, request.getSymbol(), payload);
            LOGGER.info("[producer] order queued for {}: {}", request.getSymbol(), payload);
        } catch (Exception e) {
            LOGGER.error("[producer] failed to send order: {}", e.getMessage());
            throw new RuntimeException("Failed to queue order", e);
        }
    }
}

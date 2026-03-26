package com.tradex.messaging.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tradex.dto.request.OrderRequest;
import com.tradex.enums.OrderAction;
import com.tradex.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class OrderConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderConsumer.class);

    @Autowired
    private OrderService orderService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @KafkaListener(topics = "order-events", groupId = "order-processor")
    public void consume(String message) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = objectMapper.readValue(message, Map.class);

            int accountNumber = (int) data.get("accountNumber");
            OrderRequest request = new OrderRequest();
            request.setSymbol((String) data.get("symbol"));
            request.setUnitPrice(((Number) data.get("unitPrice")).doubleValue());
            request.setQuantity(((Number) data.get("quantity")).doubleValue());
            request.setAction(OrderAction.valueOf((String) data.get("action")));

            String orderId = orderService.createOrder(accountNumber, request);
            LOGGER.info("[consumer] order executed: {}", orderId);

            // Notify frontend via WebSocket
            messagingTemplate.convertAndSend(
                "/topic/order-executed/" + accountNumber,
                Map.of("orderId", orderId, "symbol", request.getSymbol(), "action", request.getAction().name())
            );
        } catch (Exception e) {
            LOGGER.error("[consumer] failed to process order: {}", e.getMessage());
        }
    }
}

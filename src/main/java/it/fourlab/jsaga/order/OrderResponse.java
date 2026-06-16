package it.fourlab.jsaga.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long id,
        Long eventId,
        List<OrderItemResponse> items,
        BigDecimal totalAmount,
        String paymentMethod,
        LocalDateTime createdAt,
        boolean printed) {
}

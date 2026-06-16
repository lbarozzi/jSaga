package it.fourlab.jsaga.order;

import java.math.BigDecimal;

public record OrderItemResponse(
        Long productId,
        String productName,
        int qty,
        BigDecimal unitPrice,
        BigDecimal lineTotal) {
}

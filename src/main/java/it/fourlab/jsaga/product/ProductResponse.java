package it.fourlab.jsaga.product;

import java.math.BigDecimal;

public record ProductResponse(
        Long id,
        String name,
        ProductCategory category,
        BigDecimal price,
        boolean active) {
}

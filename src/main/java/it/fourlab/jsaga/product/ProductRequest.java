package it.fourlab.jsaga.product;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProductRequest(
        @NotBlank @Size(max = 120) String name,
        @NotNull ProductCategory category,
        @NotNull @DecimalMin(value = "0.01") BigDecimal price,
        @NotNull Boolean active) {
}

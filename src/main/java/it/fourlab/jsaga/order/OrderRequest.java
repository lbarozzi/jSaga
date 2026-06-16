package it.fourlab.jsaga.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record OrderRequest(
        Long eventId,
        @NotEmpty List<@Valid OrderItemRequest> items,
        @NotBlank @Size(max = 32) String paymentMethod) {
}

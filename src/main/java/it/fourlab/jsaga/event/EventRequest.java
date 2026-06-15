package it.fourlab.jsaga.event;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;


public record EventRequest(
        @NotBlank @Size(max = 120) String name,
        @NotNull LocalDate eventDate,
        @NotNull EventStatus status) {
}

package it.fourlab.jsaga.event;

import java.time.LocalDate;

public record EventResponse(
        Long id,
        String name,
        LocalDate eventDate,
        EventStatus status) {
}

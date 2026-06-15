package it.fourlab.jsaga.event;

import it.fourlab.jsaga.common.BusinessValidationException;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;



@SpringBootTest
class EventControllerIntegrationTest {

    @Autowired
    private EventService eventService;

    @Autowired
    private EventRepository eventRepository;

    @BeforeEach
    void setUp() {
        eventRepository.deleteAll();
    }

    @Test
    void createAndListEvent() {
        EventRequest request = new EventRequest(
                "Sagra 2026",
                LocalDate.of(2026, 8, 1),
                EventStatus.APERTO);

        EventResponse created = eventService.create(request);
        List<EventResponse> events = eventService.findAll(null);

        assertThat(created.id()).isNotNull();
        assertThat(created.name()).isEqualTo("Sagra 2026");
        assertThat(events).hasSize(1);
        assertThat(events.getFirst().status()).isEqualTo(EventStatus.APERTO);
    }

    @Test
        void createOpenEventWithPastDateThrowsBusinessValidationException() {
        EventRequest request = new EventRequest(
            "Evento non valido",
            LocalDate.now().minusDays(1),
            EventStatus.APERTO);

        assertThatThrownBy(() -> eventService.create(request))
            .isInstanceOf(BusinessValidationException.class)
            .hasMessage("An open event cannot have a past date");
    }
}

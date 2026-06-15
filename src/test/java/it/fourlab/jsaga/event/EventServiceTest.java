package it.fourlab.jsaga.event;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import it.fourlab.jsaga.common.BusinessValidationException;

@SpringBootTest
class EventServiceTest {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private EventService eventService;

    @BeforeEach
    void setUp() {
        eventRepository.deleteAll();
    }

    @Test
    void findAllWithStatusFilterReturnsFilteredResults() {
        eventRepository.save(new Event("Festa d'Estate", LocalDate.of(2026, 7, 10), EventStatus.APERTO));
        eventRepository.save(new Event("Evento chiuso", LocalDate.of(2026, 7, 11), EventStatus.CHIUSO));

        List<EventResponse> results = eventService.findAll(EventStatus.APERTO);

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().name()).isEqualTo("Festa d'Estate");
        assertThat(results.getFirst().status()).isEqualTo(EventStatus.APERTO);
    }

    @Test
    void createOpenEventWithPastDateThrowsBusinessValidationException() {
        EventRequest request = new EventRequest(
                "Evento passato",
                LocalDate.now().minusDays(1),
                EventStatus.APERTO);

        assertThatThrownBy(() -> eventService.create(request))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessage("An open event cannot have a past date");

        assertThat(eventRepository.count()).isZero();
    }
}

package it.fourlab.jsaga.event;

import it.fourlab.jsaga.common.BusinessValidationException;
import it.fourlab.jsaga.common.ResourceNotFoundException;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;



@Service
public class EventService {

    private final EventRepository eventRepository;

    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Transactional(readOnly = true)
    public List<EventResponse> findAll(EventStatus status) {
        List<Event> events = status == null
                ? eventRepository.findAll()
                : eventRepository.findByStatus(status);

        return events.stream().map(EventService::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public EventResponse findById(Long id) {
        Event event = findEntityById(id);
        return toResponse(event);
    }

    @Transactional
    public EventResponse create(EventRequest request) {
        validateOpenEventDate(request.eventDate(), request.status());
        Event event = new Event(request.name().trim(), request.eventDate(), request.status());
        Event saved = eventRepository.save(event);
        return toResponse(saved);
    }

    @Transactional
    public EventResponse update(Long id, EventRequest request) {
        Event event = findEntityById(id);
        validateOpenEventDate(request.eventDate(), request.status());

        event.setName(request.name().trim());
        event.setEventDate(request.eventDate());
        event.setStatus(request.status());

        Event saved = eventRepository.save(event);
        return toResponse(saved);
    }

    @Transactional
    public EventResponse updateStatus(Long id, EventStatus status) {
        Event event = findEntityById(id);
        validateOpenEventDate(event.getEventDate(), status);
        event.setStatus(status);
        Event saved = eventRepository.save(event);
        return toResponse(saved);
    }

    @Transactional
    public void delete(Long id) {
        if (!eventRepository.existsById(id)) {
            throw new ResourceNotFoundException("Event not found: " + id);
        }
        eventRepository.deleteById(id);
    }

    private Event findEntityById(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found: " + id));
    }

    private void validateOpenEventDate(LocalDate eventDate, EventStatus status) {
        if (status == EventStatus.APERTO && eventDate.isBefore(LocalDate.now())) {
            throw new BusinessValidationException("An open event cannot have a past date");
        }
    }

    private static EventResponse toResponse(Event event) {
        return new EventResponse(
                event.getId(),
                event.getName(),
                event.getEventDate(),
                event.getStatus());
    }
}

package ru.practicum.request.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.enumerations.RequestStatus;
import ru.practicum.event.model.Event;
import ru.practicum.request.model.EventRequest;
import ru.practicum.user.model.User;

import java.util.List;

public interface EventRequestRepository extends JpaRepository<EventRequest, Long> {

    List<EventRequest> findAllByRequesterIdAndEventId(Long userId, Long eventId);

    List<EventRequest> findAllByEventId(Long eventId);

    List<EventRequest> findAllByRequester(User requester);

    List<EventRequest> findAllByIdIn(List<Long> ids);

    Long countByEventIdAndStatus(Long eventId, RequestStatus status);

    List<EventRequest> findAllByEventInAndStatus(List<Event> events, RequestStatus status);
}

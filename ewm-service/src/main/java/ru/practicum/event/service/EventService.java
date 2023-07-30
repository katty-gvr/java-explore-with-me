package ru.practicum.event.service;

import ru.practicum.enumerations.EventState;
import ru.practicum.event.dto.*;
import ru.practicum.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.dto.ParticipationRequestDto;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface EventService {

    EventDto addNewEvent(NewEventDto newEventDto, Long userId);

    Collection<EventShortDto> getAllByUserId(Long userId, Integer from, Integer size);

    EventDto getEventByInitiatorIdAndEventId(Long initiatorId, Long eventId);

    EventDto updateEventByInitiator(Long initiatorId, Long eventId, EventDtoForUserUpdate eventDtoForUserUpdate);

    EventDto updateEventByAdmin(Long eventId, EventDtoForAdminUpdate eventDtoForAdminUpdate);

    Collection<EventDto> searchEvents(List<Long> users, List<EventState> states, List<Long> categories,
                                      LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                      Integer from, Integer size);

    Collection<ParticipationRequestDto> getRequestsByUserAndEvent(Long userId, Long eventId);

    EventRequestStatusUpdateResult updateRequestStatus(Long userId, Long eventId,
                                                       EventRequestStatusUpdateRequest statusUpdateRequest);

    EventDto getPublicEventById(Long eventId, String url, String ip);

    Collection<EventDto> getPublicEventsByFilters(String text, List<Long> categories, Boolean paid,
                                                  LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                                  Boolean onlyAvailable, String sort,
                                                  Integer from, Integer size, String url, String ip);
}

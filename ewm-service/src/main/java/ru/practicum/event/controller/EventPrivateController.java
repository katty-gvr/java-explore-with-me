package ru.practicum.event.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.dto.EventDto;
import ru.practicum.event.dto.EventDtoForUserUpdate;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.dto.NewEventDto;
import ru.practicum.event.service.EventService;
import ru.practicum.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.dto.ParticipationRequestDto;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.Collection;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/users")
public class EventPrivateController {
    private final EventService eventService;

    @PostMapping("/{userId}/events")
    @ResponseStatus(HttpStatus.CREATED)
    public EventDto addEvent(@Valid @RequestBody NewEventDto newEventDto,
                             @PathVariable @Positive final Long userId) {
        return eventService.addNewEvent(newEventDto, userId);
    }

    @GetMapping("/{userId}/events")
    @ResponseStatus(HttpStatus.OK)
    public Collection<EventShortDto> getUserEvents(@PathVariable @Positive final Long userId,
                                                   @RequestParam(value = "from", required = false, defaultValue = "0")
                                                   @PositiveOrZero(message = "Значение 'from' должно быть положительным")
                                                   final Integer from,
                                                   @RequestParam(value = "size", required = false, defaultValue = "10")
                                                   @Positive(message = "Значение 'size' должно быть положительным")
                                                   final Integer size) {
        return eventService.getAllByUserId(userId, from, size);
    }

    @GetMapping("/{userId}/events/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public EventDto getEventByInitiatorAndId(@PathVariable @Positive final Long userId,
                                             @PathVariable @Positive final Long eventId) {
        return eventService.getEventByInitiatorIdAndEventId(userId, eventId);
    }

    @PatchMapping("/{userId}/events/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public EventDto updateEventByInitiator(@PathVariable @Positive final Long userId,
                                           @PathVariable @Positive final Long eventId,
                                           @RequestBody @Validated EventDtoForUserUpdate dtoForUserUpdate) {
        return eventService.updateEventByInitiator(userId, eventId, dtoForUserUpdate);
    }

    @GetMapping("/{userId}/events/{eventId}/requests")
    @ResponseStatus(HttpStatus.OK)
    public Collection<ParticipationRequestDto> getUserRequests(@PathVariable final Long userId,
                                                               @PathVariable final Long eventId) {
        return eventService.getRequestsByUserAndEvent(userId, eventId);
    }

    @PatchMapping("/{userId}/events/{eventId}/requests")
    @ResponseStatus(HttpStatus.OK)
    public EventRequestStatusUpdateResult updateRequestsStatuses(@PathVariable final Long userId,
                                                                 @PathVariable final Long eventId,
                                                                 @RequestBody
                                                                 EventRequestStatusUpdateRequest request) {
        return eventService.updateRequestStatus(userId, eventId, request);
    }
}

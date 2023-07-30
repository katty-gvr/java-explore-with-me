package ru.practicum.request.service;

import ru.practicum.request.dto.ParticipationRequestDto;

import java.util.Collection;

public interface EventRequestService {

    ParticipationRequestDto addNewRequest(Long userId, Long eventId);

    ParticipationRequestDto cancelRequest(Long userId, Long requestId);

    Collection<ParticipationRequestDto> getAllUserRequests(Long userId);
}

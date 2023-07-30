package ru.practicum.request.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.enumerations.RequestStatus;

@Data
@Builder
public class ParticipationRequestDto {
    String created;
    Long event;
    Long requester;
    Long id;
    RequestStatus status;
}

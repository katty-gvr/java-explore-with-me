package ru.practicum.request.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.enumerations.RequestStatus;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@Builder
public class EventRequestStatusUpdateRequest {
    @NotEmpty
    List<Long> requestIds;

    @NotNull
    RequestStatus status;
}

package ru.practicum.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.client.StatClient;
import ru.practicum.comment.model.Comment;
import ru.practicum.comment.repository.CommentRepository;
import ru.practicum.dto.HitDto;
import ru.practicum.dto.StatDto;
import ru.practicum.enumerations.AdminStateEventAction;
import ru.practicum.enumerations.EventState;
import ru.practicum.enumerations.RequestStatus;
import ru.practicum.enumerations.UserStateEventAction;
import ru.practicum.event.dto.*;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.mapper.LocationMapper;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.Location;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.event.repository.LocationRepository;
import ru.practicum.exception.BadRequestException;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.TimeValidationException;
import ru.practicum.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.mapper.EventRequestMapper;
import ru.practicum.request.model.EventRequest;
import ru.practicum.request.repository.EventRequestRepository;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import javax.persistence.criteria.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static ru.practicum.enumerations.RequestStatus.CONFIRMED;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    @Value("${app-name}")
    private String nameOfService;
    private final EventRepository eventRepository;
    private final LocationRepository locationRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final EventRequestRepository requestRepository;
    private final CommentRepository commentRepository;
    private final StatClient statClient;

    @Override
    @Transactional
    public EventDto addNewEvent(NewEventDto newEventDto, Long userId) {

        User initiator = getUserById(userId);
        Category category = getEventCategory(newEventDto.getCategory());
        LocationDto locationDto = newEventDto.getLocation();

        Event eventForSave = EventMapper.toEvent(newEventDto, category, initiator);
        checkEventTime(eventForSave.getEventDate());
        eventForSave.setLocation(locationRepository.save(LocationMapper.toLocation(locationDto)));

        log.info("Пользователь с id {} добавил событие {}", userId, eventForSave);
        return EventMapper.toEventFullDto(eventRepository.save(eventForSave));
    }

    @Override
    public Collection<EventShortDto> getAllByUserId(Long userId, Integer from, Integer size) {
        User initiator = getUserById(userId);
        Pageable pageable = PageRequest.of(from / size, size, Sort.by("id").ascending());
        List<Event> eventsOfUser = eventRepository.findAllByInitiator(initiator, pageable);

        Map<Event, Long> eventsComments = getEventsComments(eventsOfUser);
        for (Event event : eventsOfUser) {
            event.setComments(eventsComments.get(event));
        }

        log.info("Получение событий пользователя с id {} и параметрами from {} и size {}", userId, from, size);
        return eventsOfUser.stream().map(EventMapper::toEventShortDto).collect(Collectors.toList());
    }

    @Override
    public EventDto getEventByInitiatorIdAndEventId(Long initiatorId, Long eventId) {
        User initiator = getUserById(initiatorId);
        eventRepository.findById(eventId).orElseThrow(() ->
                new NotFoundException(String.format("Событие с id=%d не найдено", eventId)));
        Event event = eventRepository.findByInitiatorAndId(initiator, eventId);
        event.setComments((long) commentRepository.findAllByEventIn(List.of(event)).size());

        log.info("Получена подробная информация о событии с id {} пользователем с id {}", eventId, initiatorId);
        //return EventMapper.toEventFullDto(eventRepository.findByInitiatorAndId(initiator, eventId));
        return EventMapper.toEventFullDto(event);
    }

    @Override
    @Transactional
    public EventDto updateEventByInitiator(Long initiatorId, Long eventId, EventDtoForUserUpdate dtoForUserUpdate) {
        User initiator = getUserById(initiatorId);
        Event event = eventRepository.findByInitiatorAndId(initiator, eventId);

        String newAnnotation = dtoForUserUpdate.getAnnotation();
        Long newCategory = dtoForUserUpdate.getCategory();
        String newDescription = dtoForUserUpdate.getDescription();
        LocalDateTime newEventDate = dtoForUserUpdate.getEventDate();
        LocationDto newLocation = dtoForUserUpdate.getLocation();
        Boolean newPaid = dtoForUserUpdate.getPaid();
        Long newParticipantLimit = dtoForUserUpdate.getParticipantLimit();
        Boolean newRequestModeration = dtoForUserUpdate.getRequestModeration();
        UserStateEventAction newStateAction = dtoForUserUpdate.getStateAction();
        String newTitle = dtoForUserUpdate.getTitle();

        if (!initiatorId.equals(event.getInitiator().getId())) {
            throw new ConflictException("Редакторирование события доступно только инициатору этого события!");
        }
        if (event.getState().equals(EventState.PUBLISHED)) {
            throw new ConflictException("Редакторирование недоступно для опубликованных событий!");
        }
        if (newEventDate != null) {
            checkEventTime(newEventDate);
            event.setEventDate(newEventDate);
        }
        if (newCategory != null) {
            event.setCategory(getEventCategory(newCategory));
        }
        if (newAnnotation != null && !newAnnotation.isBlank()) {
            event.setAnnotation(newAnnotation);
        }
        if (newDescription != null && !newDescription.isBlank()) {
            event.setDescription(newDescription);
        }
        if (newLocation != null) {
            event.setLocation(LocationMapper.toLocation(newLocation));
        }
        if (newPaid != null) {
            event.setPaid(newPaid);
        }
        if (newParticipantLimit != null) {
            event.setParticipantLimit(newParticipantLimit);
        }
        if (newRequestModeration != null) {
            event.setRequestModeration(newRequestModeration);
        }
        if (newTitle != null && !newTitle.isBlank()) {
            event.setTitle(newTitle);
        }
        if (newStateAction != null) {
            if (newStateAction.equals(UserStateEventAction.CANCEL_REVIEW)) {
                event.setState(EventState.CANCELED);
            } else {
                event.setState(EventState.PENDING);
            }
        }

        log.info("Событие {} обновлено пользователем {}", event, initiator);
        return EventMapper.toEventFullDto(event);
    }

    @Override
    @Transactional
    public EventDto updateEventByAdmin(Long eventId, EventDtoForAdminUpdate dtoForAdminUpdate) {
        Event event = getEventById(eventId);

        String newAnnotation = dtoForAdminUpdate.getAnnotation();
        Long newCategory = dtoForAdminUpdate.getCategory();
        String newDescription = dtoForAdminUpdate.getDescription();
        LocalDateTime newEventDate = dtoForAdminUpdate.getEventDate();
        LocationDto newLocation = dtoForAdminUpdate.getLocation();
        Boolean newPaid = dtoForAdminUpdate.getPaid();
        Long newParticipantLimit = dtoForAdminUpdate.getParticipantLimit();
        Boolean newRequestModeration = dtoForAdminUpdate.getRequestModeration();
        AdminStateEventAction newStateAction = dtoForAdminUpdate.getStateAction();
        String newTitle = dtoForAdminUpdate.getTitle();

        if (newStateAction == AdminStateEventAction.PUBLISH_EVENT) { // публикация события
            if (event.getState() != EventState.PENDING) {
                throw new ConflictException(String.format("Невозможно опубликовать событие с id = %d, " +
                        "так как оно не находится в стадии ожидания", eventId));
            }
            LocalDateTime eventPublished = LocalDateTime.now();
            event.setPublishedOn(eventPublished);
            event.setState(EventState.PUBLISHED);
        }

        if (newStateAction == AdminStateEventAction.REJECT_EVENT) { // отклонение события
            if (event.getState() == EventState.PUBLISHED && event.getPublishedOn().isBefore(LocalDateTime.now())) {
                throw new ConflictException("Не возможно опубликовать событие с ИД: " + eventId);
            }
            event.setState(EventState.CANCELED);
        }

        if (newAnnotation != null) {
            event.setAnnotation(newAnnotation);
        }
        if (newCategory != null) {
            event.setCategory(getEventCategory(newCategory));
        }
        if (newDescription != null) {
            event.setDescription(newDescription);
        }
        if (newEventDate != null) {
            checkEventTime(newEventDate);
            event.setEventDate(newEventDate);
        }
        if (newLocation != null) {
            Location location = LocationMapper.toLocation(newLocation);
            locationRepository.save(location);
            event.setLocation(location);
        }
        if (newParticipantLimit != null) {
            event.setParticipantLimit(newParticipantLimit);
        }
        if (newPaid != null) {
            event.setPaid(newPaid);
        }
        if (newRequestModeration != null) {
            event.setRequestModeration(newRequestModeration);
        }
        if (newTitle != null) {
            event.setTitle(newTitle);
        }
        eventRepository.save(event);
        return EventMapper.toEventFullDto(event);
    }

    @Override
    public Collection<EventDto> searchEvents(List<Long> users, List<EventState> states, List<Long> categories,
                                          LocalDateTime rangeStart, LocalDateTime rangeEnd, Integer from, Integer size) {

        Pageable pageable = PageRequest.of(from / size, size);

        List<EventDto> foundEventDtoList = new ArrayList<>();

        Specification<Event> specification = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (users != null && !users.isEmpty()) {
                predicates.add(criteriaBuilder.and(root.get("initiator").get("id").in(users)));
            }
            if (states != null && !states.isEmpty()) {
                predicates.add(criteriaBuilder.and(root.get("state").in(states)));
            }
            if (categories != null && !categories.isEmpty()) {
                predicates.add(criteriaBuilder.and(root.get("category").get("id").in(categories)));
            }
            if (rangeStart != null) {
                predicates.add(criteriaBuilder.and(criteriaBuilder.greaterThan(root.get("eventDate"), rangeStart)));
            }
            if (rangeEnd != null) {
                predicates.add(criteriaBuilder.and(criteriaBuilder.lessThan(root.get("eventDate"), rangeEnd)));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        List<Event> events = eventRepository.findAll(specification, pageable);

        Map<Event, Long> confirmedRequests = getConfirmedRequestsForEventList(events); // выгрузили подтвержденные запросы событий
        List<StatDto> eventsStatistic = getStatisticForEventList(events); // выгрузили статистику событий
        Map<String, Long> eventViews = loadViewsToEventList(eventsStatistic); // выгрузили просмотры
        Map<Event, Long> eventsComments = getEventsComments(events);

        for (Event event : events) {
            event.setConfirmedRequests(confirmedRequests.get(event));
            event.setViews(eventViews.get(String.format("/events/%s", event.getId())));
            foundEventDtoList.add(EventMapper.toEventFullDto(event));
            event.setComments(eventsComments.get(event));
        }

        log.info("Найдены события по следующим параметрам: users {}, states {}, categories {}, rangeStart {}, rangeEnd {}" +
                "from {}, size {}", users, states, categories, rangeStart, rangeEnd, from, size);
        return foundEventDtoList;
    }

    @Override
    public EventDto getPublicEventById(Long eventId, String url, String ip) {
        Event event = getEventById(eventId);
        StatDto statDto = addNewHit(url, ip);

        if (event.getState() != EventState.PUBLISHED) {
            throw new NotFoundException(String.format("Событие с id = %d не найдено среди опубликованных", eventId));
        }

        loadConfirmedRequests(event);
        event.setViews(Objects.requireNonNullElseGet(statDto, () -> Objects.requireNonNull(getEventStatistic(event))).getHits());
        event.setComments((long) commentRepository.findAllByEventIn(List.of(event)).size());

        log.info("Выполнен запрос к событию {}, url {}, ip {}", event, url, ip);
        return EventMapper.toEventFullDto(event);
    }

    @Override
    public Collection<EventDto> getPublicEventsByFilters(String text, List<Long> categories, Boolean paid,
                                                         LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                                         Boolean onlyAvailable, String sort,
                                                         Integer from, Integer size, String url, String ip) {
        addNewHit(url, ip);
        Sort sorting = defineSorting(sort);
        Pageable pageable = PageRequest.of(from, size, sorting);

        Specification<Event> specification = ((root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            Subquery<Long> subQuery = query.subquery(Long.class);
            Root<EventRequest> requestRoot = subQuery.from(EventRequest.class);
            Join<EventRequest, Event> eventJoin = requestRoot.join("event");

            predicates.add(criteriaBuilder.equal(root.get("state"), EventState.PUBLISHED));

            if (text != null && !text.isEmpty()) {
                String searchTextLower = "%" + text.toLowerCase() + "%";
                Predicate annotationLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("annotation")), searchTextLower);
                Predicate descriptionLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), searchTextLower);
                predicates.add(criteriaBuilder.or(annotationLike, descriptionLike));
            }

            if (categories != null && !categories.isEmpty()) {
                for (Long catId : categories) {
                    if (catId <= 0) {
                        throw new BadRequestException("ID категории должно быть задано корректно");
                    }
                }
                predicates.add(root.get("category").in(categories));
            }

            if (paid != null) {
                predicates.add(criteriaBuilder.equal(root.get("paid"), paid));
            }

            if (rangeStart != null && rangeEnd != null) {
                predicates.add(criteriaBuilder.greaterThan(root.get("eventDate"), rangeStart));
                predicates.add(criteriaBuilder.lessThan(root.get("eventDate"), rangeEnd));
            } else {
                predicates.add(criteriaBuilder.greaterThan(root.get("eventDate"), LocalDateTime.now()));
            }

            if (onlyAvailable != null && onlyAvailable) {
                predicates.add(criteriaBuilder.or(criteriaBuilder.equal(root.get("participantLimit"), 0),
                        criteriaBuilder.and(criteriaBuilder.notEqual(root.get("participantLimit"), 0),
                                criteriaBuilder.greaterThan(root.get("participantLimit"),
                                        subQuery.select(criteriaBuilder.count(requestRoot.get("id")))
                                                .where(criteriaBuilder.equal(eventJoin.get("id"), requestRoot.get("event").get("id")))
                                                .where(criteriaBuilder.equal(requestRoot.get("status"), CONFIRMED))))));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        });

        List<Event> foundEvents = eventRepository.findAll(specification, pageable);
        Map<Event, Long> confirmedRequests = getConfirmedRequestsForEventList(foundEvents);
        Map<Event, Long> eventsComments = getEventsComments(foundEvents);

        for (Event event : foundEvents) {
            event.setConfirmedRequests(confirmedRequests.get(event));
            event.setComments(eventsComments.get(event));
        }

        List<StatDto> eventsStatistic = getStatisticForEventList(foundEvents);
        Map<String, Long> eventViews = loadViewsToEventList(eventsStatistic);

        log.info("Выполнен запрос на поиск события по параметрам: text {}, categories {}, paid {}, rangeStart {}, " +
                        "rangeEnd {}, onlyAvailable {}, from {}, size {}, ip {}, url {}",
                text, categories, paid, rangeStart, rangeEnd, onlyAvailable, from, size, ip, url);

        return EventMapper.toEventDtoListWithViews(foundEvents, eventViews);
    }

    @Override
    public Collection<ParticipationRequestDto> getRequestsByUserAndEvent(Long userId, Long eventId) {
        User user = getUserById(userId);
        Event event = getEventById(eventId);

        if (!user.getId().equals(event.getInitiator().getId())) {
            throw new ConflictException("Получить информацию о запросах на участие в этом событии может только его инициатор!");
        }
        List<EventRequest> userRequests = requestRepository.findAllByEventId(eventId);
        log.info("Получен список запросов пользователя с id = {} на событие с id = {}", userId, eventId);
        return userRequests.stream().map(EventRequestMapper::toRequestDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateRequestStatus(Long userId, Long eventId,
                                                              EventRequestStatusUpdateRequest statusUpdateRequest) {
        User user = getUserById(userId);
        Event event = getEventById(eventId);

        if (!user.getId().equals(event.getInitiator().getId())) {
            throw new ConflictException("Обновить информацию о запросах на участие в этом событии может только его инициатор!");
        }

        if (event.getParticipantLimit() != 0) {
            List<EventRequest> eventRequests = requestRepository.findAllByEventId(eventId);
            if (event.getParticipantLimit() <= eventRequests.size()) {
                throw new ConflictException("Достигнут лимит запросов на участие в этом событии!");
            }
        }

        List<EventRequest> requestsByIds = requestRepository.findAllByIdIn(statusUpdateRequest.getRequestIds());
        List<EventRequest> requestsToUpdate = new ArrayList<>();
        List<ParticipationRequestDto> confirmedRequests = new ArrayList<>();
        List<ParticipationRequestDto> rejectedRequests = new ArrayList<>();

        for (EventRequest request : requestsByIds) {
            if (!request.getEvent().getId().equals(eventId)) {
                throw new ConflictException("Запрос не относится к событию!");
            }
            if (!request.getStatus().equals(RequestStatus.PENDING)) {
                throw new ConflictException("Статус запроса не является ожидающим подтверждения!");
            }
            requestsToUpdate.add(request);
        }

        for (EventRequest request : requestsToUpdate) {
            request.setStatus(statusUpdateRequest.getStatus());
            if (statusUpdateRequest.getStatus().equals(CONFIRMED)) {
                log.info("Пользователь с id {} подтвердил запрос с id {} на участие в событии с id {}", userId, request.getId(), eventId);
                confirmedRequests.add(EventRequestMapper.toRequestDto(request));
            } else if (statusUpdateRequest.getStatus().equals(RequestStatus.REJECTED)) {
                log.info("Пользователь с id {} отклонил запрос с id {} на участие в событии с id {}", userId, request.getId(), eventId);
                rejectedRequests.add(EventRequestMapper.toRequestDto(request));
            }
        }
        requestRepository.saveAll(requestsToUpdate);

        return EventRequestStatusUpdateResult.builder()
                .confirmedRequests(confirmedRequests)
                .rejectedRequests(rejectedRequests)
                .build();
    }

    private Map<Event, Long> getConfirmedRequestsForEventList(List<Event> events) {
        return requestRepository.findAllByEventInAndStatus(events, RequestStatus.CONFIRMED)
                .stream()
                .collect(groupingBy(EventRequest::getEvent, counting()));
    }

    private StatDto getEventStatistic(Event event) {
        String eventUri = String.format("/events/%s", event.getId());
        LocalDateTime start = event.getPublishedOn();
        LocalDateTime end = LocalDateTime.now();
        List<StatDto> statistics = statClient.getStatistics(start, end, List.of(eventUri), true);

        log.info("Получена статистика о событии {}", event);
        return statistics.isEmpty() ? null : statistics.get(0);
    }

    private List<StatDto> getStatisticForEventList(List<Event> events) {
        List<String> eventUris = events
                .stream()
                .map(e -> String.format("/events/%s", e.getId()))
                .collect(Collectors.toList());
        LocalDateTime start = events.get(0).getPublishedOn();
        LocalDateTime end = LocalDateTime.now();

        log.info("Получена статистика о событиях {}", events);
        return statClient.getStatistics(start, end, eventUris, false);
    }

    private Map<String, Long> loadViewsToEventList(List<StatDto> eventsStatistic) {
        Map<String, Long> eventsViews = new HashMap<>();
        for (StatDto viewStat : eventsStatistic) {
            eventsViews.put(viewStat.getUri(), viewStat.getHits());
        }
        return eventsViews;
    }

    private void loadConfirmedRequests(Event event) {
        Long confirmedRequestsCount = requestRepository.countByEventIdAndStatus(event.getId(), RequestStatus.CONFIRMED);
        event.setConfirmedRequests(confirmedRequestsCount);
    }

    private Map<Event, Long> getEventsComments(List<Event> events) {
        return commentRepository.findAllByEventIn(events)
                .stream()
                .collect(groupingBy(Comment::getEvent, counting()));
    }

    private Sort defineSorting(String sort) {
        Sort sorting;
        if (sort != null) {
            switch (sort) {
                case "EVENT_DATE":
                    sorting = Sort.by(Sort.Direction.DESC, "eventDate");
                    break;
                case "VIEWS":
                    sorting = Sort.by(Sort.Direction.DESC, "views");
                    break;
                default:
                    sorting = Sort.by(Sort.Direction.DESC, "id");
            }
        } else {
            sorting = Sort.by(Sort.Direction.DESC, "id");
        }
        return sorting;
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException(String.format("Пользователь с id=%d не найден", userId)));
    }

    private Event getEventById(Long eventId) {
        return eventRepository.findById(eventId).orElseThrow(() ->
                new NotFoundException(String.format("Событие с id=%d не найдено", eventId)));
    }

    private Category getEventCategory(Long catId) {
        return categoryRepository.findById(catId).orElseThrow(() ->
                new NotFoundException(String.format("Категория с id=%d не найдена", catId)));
    }

    private void checkEventTime(LocalDateTime eventTime) {
        if (eventTime.isBefore(LocalDateTime.now().plusHours(2))) {
            throw new TimeValidationException("Событие должно начинаться не ранее, чем через 2 часа от текущего момента");
        }
    }

    private StatDto addNewHit(String url, String ip) {
        HitDto newHitDto = HitDto.builder()
                .app(nameOfService)
                .uri(url)
                .ip(ip)
                .timestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .build();
        return statClient.saveHit(newHitDto);
    }
}
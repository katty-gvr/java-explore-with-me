package ru.practicum.comment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.CommentUpdateDto;
import ru.practicum.comment.dto.NewCommentDto;
import ru.practicum.comment.mapper.CommentMapper;
import ru.practicum.comment.model.Comment;
import ru.practicum.comment.repository.CommentRepository;
import ru.practicum.enumerations.EventState;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Override
    @Transactional
    public CommentDto addNewComment(Long userId, NewCommentDto newCommentDto) {
        User commentator = getUserById(userId);
        Event event = getEventById(newCommentDto.getEventId());

        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new ConflictException("Нельзя оставить комменатрий к неопубликованному событию!");
        }

        Comment comment = CommentMapper.toComment(newCommentDto, commentator, event);
        log.info("Пользователь с id {} оставил комменатрий к событию с id {} с текстом {}", userId, event.getId(),
                newCommentDto.getText());
        return CommentMapper.toCommentDto(commentRepository.save(comment));
    }

    @Override
    @Transactional
    public CommentDto updateComment(Long userId, Long commentId, CommentUpdateDto commentUpdateDto) {
        Comment comment = getCommentById(commentId);

        User commentator = getUserById(userId);
        if (!comment.getCommentator().getId().equals(commentator.getId())) {
            throw new ConflictException("Обновить комменатрий может только его автор!");
        }
        comment.setText(commentUpdateDto.getText());
        comment.setUpdated(LocalDateTime.now());
        log.info("Пользователь с id={} обновил свой комменатарий с id={}. Обновленный текст комменатария: {}",
                userId, commentId, commentUpdateDto.getText());
        return CommentMapper.toCommentDto(comment);
    }

    @Override
    @Transactional
    public void deleteCommentByUser(Long userId, Long commentId) {
        Comment comment = getCommentById(commentId);

        User commentator = getUserById(userId);
        if (!comment.getCommentator().getId().equals(commentator.getId())) {
            throw new ConflictException("Удалить комменатрий может только его автор!");
        }
        log.info("Пользователь с id {} удалил комменатарий с id {}", userId, commentId);
        commentRepository.deleteById(commentId);
    }

    @Override
    @Transactional
    public void deleteCommentByAdmin(Long commentId) {
        log.info("Администратором удален комментарий с id {}", commentId);
        commentRepository.deleteById(commentId);
    }

    @Override
    public CommentDto getCommentForAdmin(Long commentId) {
        log.info("Администратором получен комментарий с id {}", commentId);
        return CommentMapper.toCommentDto(getCommentById(commentId));
    }

    @Override
    public Collection<CommentDto> getAllUserCommentsForAdmin(Long userId, Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from / size, size);
        User commentator = getUserById(userId);
        List<Comment> userComments = commentRepository.findAllByCommentator(commentator, pageable);

        log.info("Администратором получены все комментарии пользователя с id {}", userId);
        return userComments.stream().map(CommentMapper::toCommentDto).collect(Collectors.toList());
    }

    @Override
    public Collection<CommentDto> getEventComments(Long eventId, Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from / size, size, Sort.by("id").ascending());
        Event event = getEventById(eventId);

        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new ConflictException("Событие еще не опубликовано. Комментарии недоступны.");
        }
        List<Comment> eventComments = commentRepository.findAllByEvent(event, pageable);

        log.info("Получены все комментарии к событию с id {}", eventId);
        return eventComments.stream().map(CommentMapper::toCommentDto).collect(Collectors.toList());
    }


    private User getUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException(String.format("Пользователь с id=%d не найден", userId)));
    }

    private Event getEventById(Long eventId) {
        return eventRepository.findById(eventId).orElseThrow(() ->
                new NotFoundException(String.format("Событие с id=%d не найдено", eventId)));
    }

    private Comment getCommentById(Long commentId) {
        return commentRepository.findById(commentId).orElseThrow(() ->
                new NotFoundException(String.format("Комментарий с id=%d не найден", commentId)));
    }
}

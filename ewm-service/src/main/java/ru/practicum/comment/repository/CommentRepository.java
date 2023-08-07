package ru.practicum.comment.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.comment.model.Comment;
import ru.practicum.event.model.Event;
import ru.practicum.user.model.User;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findAllByCommentator(User commentator, Pageable pageable);

    List<Comment> findAllByEvent(Event event, Pageable pageable);

    List<Comment> findAllByEventIn(List<Event> events);
}

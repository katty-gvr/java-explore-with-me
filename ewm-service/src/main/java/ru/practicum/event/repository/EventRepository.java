package ru.practicum.event.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.event.model.Event;
import ru.practicum.user.model.User;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findAllByInitiator(User user, Pageable pageable);

    Event findByInitiatorAndId(User user, Long id);

    List<Event> findAll(Specification<Event> specification, Pageable pageable);

    List<Event> findAllByCategoryId(Long catId);

}

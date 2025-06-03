package ru.practicum.main.event.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.main.event.model.Event;
import ru.practicum.main.event.model.EventState;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {

    @Query("SELECT e FROM Event e " +
            "WHERE e.state = 'PUBLISHED' " +
            "AND (:text IS NULL OR (LOWER(e.annotation) LIKE LOWER(CONCAT('%', :text, '%')) OR LOWER(e.description) LIKE LOWER(CONCAT('%', :text, '%')))) " +
            "AND (:categories IS NULL OR e.category.id IN :categories) " +
            "AND (:paid IS NULL OR e.paid = :paid) " +
            "AND (COALESCE(:rangeStart, NULL) IS NULL OR e.eventDate >= :rangeStart) " +
            "AND (COALESCE(:rangeEnd, NULL) IS NULL OR e.eventDate <= :rangeEnd) " +
            "AND (e.participantLimit = 0 OR e.participantLimit > :confirmedRequests)")
    Page<Event> searchPublicEventsOnlyAvailable(
            @Param("text") String text,
            @Param("categories") List<Long> categories,
            @Param("paid") Boolean paid,
            @Param("rangeStart") LocalDateTime rangeStart,
            @Param("rangeEnd") LocalDateTime rangeEnd,
            @Param("confirmedRequests") Integer confirmedRequests,
            Pageable pageable);

    Page<Event> findByInitiatorId(Long userId, Pageable pageable);

    Optional<Event> findByIdAndInitiatorId(Long eventId, Long userId);

    Optional<Event> findByIdAndState(Long eventId, EventState state);

    List<Event> findByIdIn(List<Long> ids);

    List<Event> findByIdInAndState(List<Long> ids, EventState state);

    Page<Event> findByCategoryIdIn(List<Long> categoryIds, Pageable pageable);

    Page<Event> findByState(EventState state, Pageable pageable);

    Page<Event> findByCategoryIdAndState(Long categoryId, EventState state, Pageable pageable);

    Page<Event> findByEventDateAfter(LocalDateTime date, Pageable pageable);

    Page<Event> findByEventDateBetweenAndState(LocalDateTime start, LocalDateTime end, EventState state, Pageable pageable);

    boolean existsByCategoryId(Long categoryId);
}

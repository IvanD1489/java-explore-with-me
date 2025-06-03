package ru.practicum.main.event.repository;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import ru.practicum.main.event.model.Event;
import ru.practicum.main.event.model.EventState;

import java.time.LocalDateTime;
import java.util.List;

public class EventSpecifications {

    public static Specification<Event> adminFilter(
            List<Long> users,
            List<EventState> states,
            List<Long> categories,
            LocalDateTime rangeStart,
            LocalDateTime rangeEnd) {
        return (root, query, cb) -> {
            Predicate predicate = cb.conjunction();

            if (users != null && !users.isEmpty()) {
                predicate = cb.and(predicate, root.get("initiator").get("id").in(users));
            }
            if (states != null && !states.isEmpty()) {
                predicate = cb.and(predicate, root.get("state").in(states));
            }
            if (categories != null && !categories.isEmpty()) {
                predicate = cb.and(predicate, root.get("category").get("id").in(categories));
            }
            if (rangeStart != null) {
                predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("eventDate"), rangeStart));
            }
            if (rangeEnd != null) {
                predicate = cb.and(predicate, cb.lessThanOrEqualTo(root.get("eventDate"), rangeEnd));
            }

            return predicate;
        };
    }

    public static Specification<Event> publicFilter(
            String text,
            List<Long> categories,
            Boolean paid,
            LocalDateTime rangeStart,
            LocalDateTime rangeEnd) {
        return (root, query, cb) -> {
            Predicate predicate = cb.equal(root.get("state"), EventState.PUBLISHED);

            if (text != null && !text.isBlank()) {
                String pattern = "%" + text.toLowerCase() + "%";
                Predicate annotationLike = cb.like(cb.lower(root.get("annotation")), pattern);
                Predicate descriptionLike = cb.like(cb.lower(root.get("description")), pattern);
                predicate = cb.and(predicate, cb.or(annotationLike, descriptionLike));
            }
            if (categories != null && !categories.isEmpty()) {
                predicate = cb.and(predicate, root.get("category").get("id").in(categories));
            }
            if (paid != null) {
                predicate = cb.and(predicate, cb.equal(root.get("paid"), paid));
            }
            if (rangeStart != null) {
                predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("eventDate"), rangeStart));
            }
            if (rangeEnd != null) {
                predicate = cb.and(predicate, cb.lessThanOrEqualTo(root.get("eventDate"), rangeEnd));
            }

            return predicate;
        };
    }
}

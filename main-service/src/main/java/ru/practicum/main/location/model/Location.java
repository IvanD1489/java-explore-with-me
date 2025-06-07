package ru.practicum.main.location.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "locations", uniqueConstraints = @UniqueConstraint(columnNames = "name"))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Location {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private Float lat;

    @Column(nullable = false)
    private Float lon;

    private Float radius; // радиус зоны, в метрах

    private LocalDateTime createdOn;
}

package ru.practicum.main.location.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationDto {
    @NotNull
    private Float lat;

    @NotNull
    private Float lon;
}

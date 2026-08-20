package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Film.
 */
@Getter
@Setter
public class Film {
    private Long id;

    @NotBlank(message = "У фильма должно быть название и название не может состоять только из пробелов!")
    private String name;

    @Size(max = 200, message = "Описание не должно превышать 200 символов")
    private String description;

    @NotNull(message = "Нужно указать дату релиза")
    @PastOrPresent(message = "Нельзя указать дату релиза позже нынешней даты")
    private LocalDate releaseDate;

    @Positive(message = "Продолжительность должна быть положительным числом")
    private int duration;

    private List<Genre> genres;

    private MPA rating;

    private Set<Long> likes = new HashSet<>();
}
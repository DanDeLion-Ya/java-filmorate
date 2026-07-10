package ru.yandex.practicum.filmorate.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exception.ValidationException;

import java.time.LocalDate;

public class FilmTest {
    private FilmController filmController = new FilmController();
    private Film film;

    @BeforeEach
    void prepareFilm() {
        film = new Film();
        film.setName("Хакеры");
        film.setDescription("Фильм о хакерах, взломах и молодой Анджелине Джоли.");
        film.setReleaseDate(LocalDate.of(1995,9,15));
        film.setDuration(107);
    }

    @Test
    public void shouldCreateFilmWithValidData() {
        assertDoesNotThrow(() -> {
            filmController.validateName(film.getName());
            filmController.validateDescription(film.getDescription());
            filmController.validateReleaseDate(film.getReleaseDate());
            filmController.validateDuration(film.getDuration());
        });
    }

    @Test
    public void shouldThrowExceptionWhenNameIsNull() {
        film.setName(null);
        assertThrows(ValidationException.class,() -> {
            filmController.validateName(film.getName());
        });
    }

    @Test
    public void shouldThrowExceptionWhenNameIsBlank() {
        film.setName("");
        assertThrows(ValidationException.class,() -> {
            filmController.validateName(film.getName());
        });
    }

    @Test
    public void shouldThrowExceptionWhenNameIsOnlySpaces() {
        film.setName("   ");
        assertThrows(ValidationException.class,() -> {
            filmController.validateName(film.getName());
        });
    }

    @Test
    public void shouldThrowExceptionWhenDescriptionIsMoreSymbols() {
        film.setDescription("У".repeat(201));
        assertThrows(ValidationException.class,() -> {
            filmController.validateDescription(film.getDescription());
        });
    }

    @Test
    public void shouldThrowExceptionWhenDescriptionIs200Symbols() {
        film.setDescription("У".repeat(200));
        assertDoesNotThrow(() -> {
            filmController.validateDescription(film.getDescription());
        });
    }

    @Test
    public void shouldThrowExceptionWhenReleaseDateIsBefore() {
        film.setReleaseDate(LocalDate.of(1800,9,15));
        assertThrows(ValidationException.class,() -> {
            filmController.validateReleaseDate(film.getReleaseDate());
        });
    }

    @Test
    public void shouldThrowExceptionWhenReleaseDateIsBirthdayMovie() {
        film.setReleaseDate(LocalDate.of(1895,12,28));
        assertDoesNotThrow(() -> {
            filmController.validateReleaseDate(film.getReleaseDate());
        });
    }

    @Test
    public void shouldThrowExceptionWhenReleaseDateIsAfterNow() {
        film.setReleaseDate(LocalDate.of(2500,9,15));
        assertThrows(ValidationException.class,() -> {
            filmController.validateReleaseDate(film.getReleaseDate());
        });
    }

    @Test
    public void shouldThrowExceptionWhenDurationIsNegativeValue() {
        film.setDuration(-107);
        assertThrows(ValidationException.class,() -> {
            filmController.validateDuration(film.getDuration());
        });
    }

    @Test
    public void shouldThrowExceptionWhenDurationIsZero() {
        film.setDuration(0);
        assertThrows(ValidationException.class,() -> {
            filmController.validateDuration(film.getDuration());
        });
    }
}
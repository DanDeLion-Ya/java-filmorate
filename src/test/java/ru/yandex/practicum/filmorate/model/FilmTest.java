package ru.yandex.practicum.filmorate.model;

import static org.junit.jupiter.api.Assertions.*;

import jakarta.validation.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exception.ValidationException;

import java.time.LocalDate;
import java.util.Set;

public class FilmTest {
    private FilmController filmController = new FilmController();
    private Film film;
    private Validator validator;

    @BeforeEach
    void prepareFilm() {
        film = new Film();
        film.setName("Хакеры");
        film.setDescription("Фильм о хакерах, взломах и молодой Анджелине Джоли.");
        film.setReleaseDate(LocalDate.of(1995,9,15));
        film.setDuration(107);

        ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @Test
    public void shouldCreateFilmWithValidData() {
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertTrue(violations.isEmpty());
    }

    @Test
    public void shouldThrowExceptionWhenNameIsNull() {
        film.setName(null);
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertEquals(1 ,violations.size());

        ConstraintViolation<Film> textViolation = violations.iterator().next();
        assertEquals("У фильма должно быть название и название не может состоять только из пробелов!",
                textViolation.getMessage());
    }

    @Test
    public void shouldThrowExceptionWhenNameIsBlank() {
        film.setName("");
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertEquals(1 ,violations.size());

        ConstraintViolation<Film> textViolation = violations.iterator().next();
        assertEquals("У фильма должно быть название и название не может состоять только из пробелов!",
                textViolation.getMessage());
    }

    @Test
    public void shouldThrowExceptionWhenNameIsOnlySpaces() {
        film.setName("   ");
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertEquals(1 ,violations.size());

        ConstraintViolation<Film> textViolation = violations.iterator().next();
        assertEquals("У фильма должно быть название и название не может состоять только из пробелов!",
                textViolation.getMessage());
    }

    @Test
    public void shouldThrowExceptionWhenDescriptionIsMoreSymbols() {
        film.setDescription("У".repeat(201));
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertEquals(1 ,violations.size());

        ConstraintViolation<Film> textViolation = violations.iterator().next();
        assertEquals("Описание не должно превышать 200 символов",
                textViolation.getMessage());
    }

    @Test
    public void shouldThrowExceptionWhenDescriptionIs200Symbols() {
        film.setDescription("У".repeat(200));
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertTrue(violations.isEmpty());
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
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertEquals(1, violations.size());

        ConstraintViolation<Film> textViolation = violations.iterator().next();
        assertEquals("Нельзя указать дату релиза позже нынешней даты",
                textViolation.getMessage());
    }

    @Test
    public void shouldThrowExceptionWhenDurationIsNegativeValue() {
        film.setDuration(-107);
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertEquals(1, violations.size());

        ConstraintViolation<Film> textViolation = violations.iterator().next();
        assertEquals("Продолжительность должна быть положительным числом",
                textViolation.getMessage());
    }

    @Test
    public void shouldThrowExceptionWhenDurationIsZero() {
        film.setDuration(0);
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertEquals(1, violations.size());

        ConstraintViolation<Film> textViolation = violations.iterator().next();
        assertEquals("Продолжительность должна быть положительным числом",
                textViolation.getMessage());
    }
}
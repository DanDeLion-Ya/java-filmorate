package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/films")
public class FilmController {
    private Map<Long, Film> films = new HashMap<>();

    @PostMapping
    public Film addFilm(@RequestBody Film newFilm) {
        log.info("Попытка добавить фильм в каталог name = {}", newFilm.getName());
        validateName(newFilm.getName());
        validateDescription(newFilm.getDescription());
        validateReleaseDate(newFilm.getReleaseDate());
        validateDuration(newFilm.getDuration());

        for (Film film : films.values()) {
            if (film.getName().equals(newFilm.getName())) {
                log.warn("Невозможно добавить данный фильм! Этот фильм {} уже есть в каталоге.", newFilm.getName());
                throw new DuplicatedDataException("Такой фильм уже есть");
            }
        }
        newFilm.setId(getNextId());
        films.put(newFilm.getId(), newFilm);
        log.info("Фильм добавлен в каталог: id = {}, name = {}.", newFilm.getId(), newFilm.getName());
        return newFilm;
    }

    @PutMapping
    public Film updateFilm(@RequestBody Film updatedFilm) {
        Long currentIdFilm = updatedFilm.getId();
        log.info("Попытка обновления данных фильма {}.", updatedFilm.getName());
        if (films.get(currentIdFilm) == null) {
            log.warn("Невозможно обновить информацию о фильме {}! " +
                    "Такого фильма нет в каталоге.", updatedFilm.getName());
            throw new NotFoundException("Такого фильма нет списке");
        }

        validateName(updatedFilm.getName());
        validateDescription(updatedFilm.getDescription());
        validateReleaseDate(updatedFilm.getReleaseDate());
        validateDuration(updatedFilm.getDuration());

        films.put(currentIdFilm, updatedFilm);
        log.info("Данные о фильме {} обновлены.", updatedFilm.getName());
        return updatedFilm;
    }

    @GetMapping
    public List<Film> getAllFilms() {
        List<Film> filmsList = new ArrayList<>();
        for (Film film : films.values()) {
            filmsList.add(film);
        }
        log.info("Получение актуального списка фильмов в каталоге.");
        return filmsList;
    }

    public Long getNextId() {
        Long newId = films.keySet()
                .stream()
                .max(Long::compare)
                .orElse(0L);
        return newId + 1;
    }

//Проверка на пустоту названия
    public void validateName(String name) {
        log.info("Проверка, что название не пустое и не состоит из пробелов.");
        if (name == null || name.isBlank()) {
            log.warn("Название фильма не введено или состоит из пробелов.");
            throw new ValidationException("Название не может быть пустым и не может состоять из пробелов");
        }
        log.info("Название корректно: {}.", name);
    }

// Провеока на максимальное количество символов
    public void validateDescription(String description) {
        log.info("Проверка на максимальное количество символов. Не более 200 символов.");
        if (description != null && description.length() > 200) {
            log.warn("Описание слишком большое. Введённое количество символов больше допустимого значения.");
            throw new ValidationException("Описание не должно превышать 200 символов");
        }
        log.info("Количество символов описания оптимально: {}.", description);
    }

    // Проверка на корректность даты релиза
    public void validateReleaseDate(LocalDate releaseDate) {
        LocalDate birthdayMovie = LocalDate.of(1895, 12, 28);
        log.info("Проверка на корректность даты релиза добавляемого фильма. Не раньше {} и не позже текущей даты {}.",
                birthdayMovie, LocalDate.now());
        if (releaseDate.isBefore(birthdayMovie)) {
            log.warn("Введена дата раньше возможной. Введено: {}. Можно не раньше: {}.", releaseDate, birthdayMovie);
            throw new ValidationException("Нельзя указать дату релиза раньше, чем 28 декабря 1895 года");
        }
        if (releaseDate.isAfter(LocalDate.now())) {
            log.warn("Введена дата позже возможной. Введено: {}. Можно не позже: {}.", releaseDate, LocalDate.now());
            throw new ValidationException("Нельзя указать дату релиза позже нынешней даты");
        }
        log.info("Введена корректная дата релиза: {}.", releaseDate);
    }

// Проверка продолжительности
    public void validateDuration(int duration) {
        log.info("Проверка продолжительности. Продолжительность не должна быть отрицательным числом.");
        if (duration <= 0) {
            log.warn("Введено отрицательное число продолжительности: {}.", duration);
            throw new ValidationException("Продолжительность должна быть положительным числом");
        }
        log.info("Введено корректное число продолжительности: {}.", duration);
    }
}

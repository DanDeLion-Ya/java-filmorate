package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

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
    public Film addFilm(@Valid @RequestBody Film newFilm) {
        log.info("Попытка добавить фильм в каталог name = {}", newFilm.getName());
        validateReleaseDate(newFilm.getReleaseDate());
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
    public Film updateFilm(@Valid @RequestBody Film updatedFilm) {
        Long currentIdFilm = updatedFilm.getId();
        log.info("Попытка обновления данных фильма {}.", updatedFilm.getName());
        if (films.get(currentIdFilm) == null) {
            log.warn("Невозможно обновить информацию о фильме {}! " +
                    "Такого фильма нет в каталоге.", updatedFilm.getName());
            throw new NotFoundException("Такого фильма нет списке");
        }

        validateReleaseDate(updatedFilm.getReleaseDate());
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

    // Проверка на корректность даты релиза
    public void validateReleaseDate(LocalDate releaseDate) {
        LocalDate birthdayMovie = LocalDate.of(1895, 12, 28);
        log.info("Проверка на корректность даты релиза добавляемого фильма. Не раньше {} и не позже текущей даты {}.",
                birthdayMovie, LocalDate.now());
        if (releaseDate.isBefore(birthdayMovie)) {
            log.warn("Введена дата раньше возможной. Введено: {}. Можно не раньше: {}.", releaseDate, birthdayMovie);
            throw new ValidationException("Нельзя указать дату релиза раньше, чем 28 декабря 1895 года");
        }
        log.info("Введена корректная дата релиза: {}.", releaseDate);
    }
}

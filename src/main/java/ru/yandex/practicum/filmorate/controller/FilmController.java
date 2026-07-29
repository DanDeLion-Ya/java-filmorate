package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.time.LocalDate;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/films")
public class FilmController {
    private final FilmStorage filmStorage;
    private FilmService filmService;
    private UserService userService;

    @Autowired
    public FilmController(FilmStorage filmStorage, FilmService filmService, UserService userService) {
        this.filmStorage = filmStorage;
        this.filmService = filmService;
        this.userService = userService;
    }

    @PostMapping
    public Film addFilm(@Valid @RequestBody Film newFilm) {
        validateReleaseDate(newFilm.getReleaseDate());
        return filmStorage.addFilm(newFilm);
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film updatedFilm) {
        validateReleaseDate(updatedFilm.getReleaseDate());
        return filmStorage.updateFilm(updatedFilm);
    }

    @GetMapping
    public List<Film> getAllFilms() {
        return filmStorage.getAllFilms();
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

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable Long id, @PathVariable Long userId) {
        filmService.addLike(filmService.getFilmId(id), userService.getUserId(userId));
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable Long id, @PathVariable Long userId) {
        filmService.removeLike(filmService.getFilmId(id), userService.getUserId(userId));
    }

    @GetMapping("/popular")
    public List<Film> getPopularFilm(@RequestParam(defaultValue = "10") int count) {
        return filmService.getPopularFilm(count);
    }
}

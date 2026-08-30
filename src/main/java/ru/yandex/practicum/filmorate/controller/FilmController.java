package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/films")
public class FilmController {
    private FilmService filmService;
    private UserService userService;

    @Autowired
    public FilmController(FilmService filmService, UserService userService) {
        this.filmService = filmService;
        this.userService = userService;
    }

    @PostMapping
    public Film createFilm(@Valid @RequestBody Film newFilm) {
        return filmService.createFilm(newFilm);
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film updatedFilm) {
        return filmService.updateFilm(updatedFilm);
    }

    @GetMapping
    public List<Film> getAllFilms() {
        return filmService.getAllFilms();
    }


    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable Long id, @PathVariable Long userId) {
        filmService.addLike(filmService.getFilmById(id).getId(), userService.getUserById(userId).getId());
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void deleteLike(@PathVariable Long id, @PathVariable Long userId) {
        filmService.deleteLike(filmService.getFilmById(id).getId(), userService.getUserById(userId).getId());
    }

    @GetMapping("/popular")
    public List<Film> getPopularFilm(@RequestParam(defaultValue = "10") int count) {
        return filmService.getPopularFilm(count);
    }

    @GetMapping("/{id}")
    public Film getFilmById(@PathVariable Long id) {
        return filmService.getFilmById(id);
    }
}

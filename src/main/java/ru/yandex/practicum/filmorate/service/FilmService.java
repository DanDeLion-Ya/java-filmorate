package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    @Autowired
    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public Film addFilm(Film newFilm) {
        validateReleaseDate(newFilm.getReleaseDate());
        return filmStorage.addFilm(newFilm);
    }

    public Film updateFilm(Film updatedFilm) {
        validateReleaseDate(updatedFilm.getReleaseDate());
        return filmStorage.updateFilm(updatedFilm);
    }

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

    public void addLike(Film film, User user) {
        log.info("Попытка пользователя {} на добавление Лайка фильму {}", user, film);
        if (film.getLikes().contains(user.getId())) {
            log.warn("Пользователь уже поставил Лайк этому фильму.");
            throw new DuplicatedDataException("Пользователь уже поставил лайк этому фильму.");
        }
        log.info("Добавление лайка фильму {}", film);
        film.getLikes().add(user.getId());
    }

    public void removeLike(Film film, User user) {
        log.info("Попытка пользователя {} на удаление Лайка у фильма {}", user, film);
        if (!film.getLikes().contains(user.getId())) {
            log.warn("Пользователь не ставил Лайк этому фильму.");
            throw new NotFoundException("Нельзя удалить Лайкол, если пользователь не ставил его этому фильму.");
        }
        log.info("Удаление лайка у фильма {}", film);
        film.getLikes().remove(user.getId());
    }

    public List<Film> getPopularFilm(int count) {
        log.info("Запрос на топ {} популярных фильмов", count);
        List<Film> listFilms = new ArrayList<>(filmStorage.getAllFilms());
        if (count <= 0) {
            count = 10;
        }
        List<Film> result = listFilms.stream()
                .sorted(Comparator.comparingInt((Film film) -> film.getLikes().size()).reversed())
                .limit(count)
                .collect(Collectors.toList());
        log.info("Возвращается подборка топ {} фильмов", result.size());
        return result;
    }

    public Film getFilmId(Long id) {
        if (id == null) {
            log.warn("ID фильма не передан!");
            throw new NotFoundException("ID фильма не может быть null!");
        }
        log.info("Запрос фильма по ID: {}", id);
        return filmStorage.getFilmId(id);
    }
}

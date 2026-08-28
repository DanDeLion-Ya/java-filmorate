package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final FilmDbStorage filmDbStorage;


    @Autowired
    public FilmService(@Qualifier("FilmDbStorage") FilmStorage filmStorage,
                       FilmDbStorage filmDbStorage) {
        this.filmStorage = filmStorage;
        this.filmDbStorage = filmDbStorage;
    }

    public Film createFilm(Film newFilm) {
        validateReleaseDate(newFilm.getReleaseDate());
        validateMpa(newFilm.getMpa());
        validateGenre(newFilm);
        return filmStorage.createFilm(newFilm);
    }

    public Film updateFilm(Film updatedFilm) {
        validateReleaseDate(updatedFilm.getReleaseDate());
        validateMpa(updatedFilm.getMpa());
        validateGenre(updatedFilm);
        return filmStorage.updateFilm(updatedFilm);
    }

    public List<Film> getAllFilms() {
        return filmStorage.getAllFilms();
    }

    public void deleteFilm(long deletedFilm_id) {
        filmStorage.deleteFilm(deletedFilm_id);
    }

    public Film getFilmById(Long id) {
        if (id == null) {
            log.warn("ID фильма не передан!");
            throw new NotFoundException("ID фильма не может быть null!");
        }
        log.info("Запрос фильма по ID: {}", id);
        return filmStorage.getFilmById(id);
    }

    public void addLike(Long filmId, Long userId) {
        filmDbStorage.addLike(filmId, userId);
    }

    public void deleteLike(Long filmId, Long userId) {
        filmDbStorage.deleteLike(filmId, userId);
    }

    public List<Film> getPopularFilm(int count) {
        return filmDbStorage.getPopularFilms(count);
    }

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

    private void validateMpa(Mpa mpa) {
        if (mpa == null) {
            throw new ValidationException("MPA рейтинг не может быть null");
        }
        if (mpa.getId() == null) {
            throw new ValidationException("ID рейтинга MPA не может быть null");
        }
        if (!filmDbStorage.mpaExists(mpa.getId())) {
            throw new NotFoundException("Рейтинга MPA с ID " + mpa.getId() + " нет в списке рейтингов");
        }
    }

    private void validateGenre(Film film) {
        if (film.getGenres() == null) {
            film.setGenres(new ArrayList<>());
            return;
        }

        if (film.getGenres().isEmpty()) {
            return;
        }

        Set<Long> uniqueId = new HashSet<>();
        for (Genre genre : film.getGenres()) {
            if (genre.getId() != null) {
                uniqueId.add(genre.getId());
            }
        }

        List<Genre> uniqueGenres = new ArrayList<>();
        for (Long id : uniqueId) {
            if (!filmDbStorage.genreExists(id)) {
                throw new NotFoundException("Жанр с ID " + id + " не существует");
            }
            Genre genre = new Genre();
            genre.setId(id);
            uniqueGenres.add(genre);
        }
        film.setGenres(uniqueGenres);
    }
}
package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mapper.FilmRowMapper;
import ru.yandex.practicum.filmorate.storage.mapper.GenreRowMapper;
import ru.yandex.practicum.filmorate.storage.mapper.MpaRowMapper;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Repository
@Qualifier("FilmDbStorage")
public class FilmDbStorage implements FilmStorage {
    private JdbcTemplate jdbcTemplate;

    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Film createFilm(Film film) {
        log.info("Попытка создания фильма: {}", film.getName());
        String queryFilm = "INSERT INTO films(name, description, releaseDate, duration, rating_id) " +
                "VALUES(?, ?, ?, ?, ?);";
        Object[] params = {film.getName(), film.getDescription(), film.getReleaseDate(), film.getDuration(),
                film.getMpa().getId()};

        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(queryFilm, Statement.RETURN_GENERATED_KEYS);
            for (int idx = 0; idx < params.length; idx++) {
                ps.setObject(idx + 1, params[idx]);
            }
            return ps;
        }, keyHolder);

        Long id = keyHolder.getKey().longValue();
        if (id != null) {
            film.setId(id);
            log.info("Фильм {} с ID {} успешно создан!", film.getName(), film.getId());
        } else {
            log.error("Фильм {} создать не удалось!", film.getName());
            throw new InternalServerException("Не удалось сохранить данные");
        }

        List<Object[]> listFilmToGenre = new ArrayList<>();
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            log.info("Добавление жанров для фильма c ID: {}", id);
            for (int i = 0; i < film.getGenres().size(); i++) {
                Genre genre = film.getGenres().get(i);
                Object[] filmToGenre = new Object[]{film.getGenres().get(i).getId(), id};
                listFilmToGenre.add(filmToGenre);
            }

            String queryGenre = "INSERT INTO film_genre(genre_id, film_id) VALUES (?, ?)";
            try {
                int[] dbFilmToGenre = jdbcTemplate.batchUpdate(queryGenre, listFilmToGenre);
                log.info("Успешно добавлено жанров {} для фильма c ID: {}", dbFilmToGenre.length, id);
            } catch (DataAccessException e) {
                log.warn("Не удалось добавить жанр(-ы) для фильма с ID: {}", id);
                throw new InternalServerException("Не удалось сохранить жанры для фильма");
            }
        } else {
            log.info("Фильм {} остался без жанра(((", film.getName());
        }
        return film;
    }

    public Film getFilmById(Long id) {
        log.info("Запрос фильма по ID: {}", id);
        String query = "SELECT * FROM films WHERE id = ?;";
        try {
            Film film = jdbcTemplate.queryForObject(query, new FilmRowMapper(), id);
            String queryGenre = "SELECT * FROM genre JOIN film_genre ON genre.id = film_genre.genre_id " +
                    "WHERE film_genre.film_id = ?;";
            List<Genre> genres = jdbcTemplate.query(queryGenre, new GenreRowMapper(), id);
            film.setGenres(genres);

            if (film.getMpa() != null && film.getMpa().getId() != null) {
                String queryMpa = "SELECT * FROM mpa WHERE id = ?";
                Mpa mpa = jdbcTemplate.queryForObject(queryMpa, new MpaRowMapper(), film.getMpa().getId());
                film.setMpa(mpa);
            }
            log.info("Найдено {} жанров для фильма ID {}", genres.size(), id);
            return film;
        } catch (EmptyResultDataAccessException e) {
            log.warn("Фильм с ID {} не найден", id);
            return  null;
        }
    }

    public List<Film> getAllFilms() {
        log.info("Запрос списка всех фильмов");
        String query = "SELECT * FROM films;";
        List<Film> listFilms = jdbcTemplate.query(query, new FilmRowMapper());

        for (int i = 0; i < listFilms.size(); i++) {
            long filmId = listFilms.get(i).getId();
            String queryGenre = "SELECT * FROM genre JOIN film_genre ON genre.id = film_genre.genre_id " +
                    "WHERE film_genre.film_id = ?;";
            List<Genre> genres = jdbcTemplate.query(queryGenre, new GenreRowMapper(), filmId);
            listFilms.get(i).setGenres(genres);
        }
        log.debug("Найдено фильмов: {}", listFilms.size());
        return listFilms;
    }

    public Film updateFilm(Film film) {
        log.info("Запрос на обновление фильма с ID: {}", film.getId());
        Film filmExist = getFilmById(film.getId());
        if (filmExist == null) {
            log.warn("Фильм с ID {} не найден для обновления", film.getId());
            throw new NotFoundException("Фильм с ID " + film.getId() + " не найден");
        }
        String query = "UPDATE films SET name = ?, description = ?, releaseDate = ?, duration = ?, rating_id = ? WHERE id = ?";
        jdbcTemplate.update(query,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId()
        );
        log.info("Данные фильма с ID {} частично обновлены", film.getId());

        String queryOutGenre = "DELETE FROM film_genre WHERE film_id = ?;";
        jdbcTemplate.update(queryOutGenre, film.getId());

        log.info("Старые жанры для фильма ID {} удалены", film.getId());
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            List<Object[]> listFilmToGenre = new ArrayList<>();
            for (int i = 0; i < film.getGenres().size(); i++) {
                Genre genre = film.getGenres().get(i);
                Object[] filmToGenre = new Object[]{film.getGenres().get(i).getId(), film.getId()};
                listFilmToGenre.add(filmToGenre);
            }

            String queryInnGenre = "INSERT INTO film_genre(genre_id, film_id) VALUES (?, ?)";
            int[] dbFilmToGenre = jdbcTemplate.batchUpdate(queryInnGenre, listFilmToGenre);
            log.info("Добавлено {} жанров для фильма ID {}", dbFilmToGenre.length, film.getId());
        } else {
            log.info("Фильм ID {} обновлен без жанров", film.getId());
        }
        log.info("Фильм с ID {}, успешно обновлён", film.getId());
        return film;
    }

    public void deleteFilm(long id) {
        log.info("Запрос на удаление фильма с ID: {}", id);
        String queryOutGenre = "DELETE FROM film_genre WHERE film_id = ?;";
        jdbcTemplate.update(queryOutGenre, id);
        log.info("Удалены связи с жанрами для фильма c ID {}", id);

        String queryOutLikes = "DELETE FROM likes WHERE film_id = ?;";
        jdbcTemplate.update(queryOutLikes, id);
        log.debug("Удалены лайки для фильма ID c {}", id);

        String query = "DELETE FROM films WHERE id = ?;";
        int rowsDeletedFromFilm = jdbcTemplate.update(query, id);
        if (rowsDeletedFromFilm == 0) {
            log.warn("Фильм с ID {} не удалось найти", id);
            throw new NotFoundException("Фильма с Id " + id + "не найден!");
        }
        log.info("Фильм успешно удалён");
    }

    public void addLike(Long filmId, Long userId) {
        log.info("Попытка добавить лайк фильму c ID {} от пользователя с ID {} ", filmId, userId);
        String query = "INSERT INTO likes (film_id, user_id) VALUES (?, ?)";
        try {
            jdbcTemplate.update(query, filmId, userId);
            log.info("Лайк успешно добавлен");
        } catch (DataAccessException e) {
            log.warn("Пользователь {} уже ранее ставил лайк фильму {}", userId, filmId);
            throw new DuplicatedDataException("Пользователь уже поставил лайк этому фильму");
        }
    }

    public void deleteLike(Long filmId, Long userId) {
        log.info("Попытка пользователя с ID {} удалить лайк у фильма c ID {}", userId, filmId);
        String query = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";
        int rowsDeleted = jdbcTemplate.update(query, filmId, userId);
        if (rowsDeleted == 0) {
            log.warn("Пользователь не ставил лайк этому фильму");
            throw new NotFoundException("Нельзя удалить лайк, если пользователь не ставил лайк фильму");
        }
        log.info("Лайк успешно удалён");
    }

    public List<Film> getPopularFilms(int count) {
        if (count <= 0) {
            count = 10;
        }
        log.info("Запрос топ {} популярных фильмов (по лайкам)", count);
        String query = "SELECT films.* FROM films LEFT OUTER JOIN likes ON films.id = likes.film_id " +
                "GROUP BY films.id ORDER BY COUNT(likes.user_id) DESC " +
                "LIMIT ?";
        List<Film> topFilms = jdbcTemplate.query(query, new FilmRowMapper(), count);

        for (Film film : topFilms) {
            String queryGenre = "SELECT * FROM genre JOIN film_genre ON genre.id = film_genre.genre_id " +
                    "WHERE film_genre.film_id = ?";

            List<Genre> genres = jdbcTemplate.query(queryGenre, new GenreRowMapper(), film.getId());
            film.setGenres(genres);
        }
        log.info("Выдаётся топ {} популярных фильмов", topFilms.size());
        return topFilms;
    }

    public boolean mpaExists(Long mpaId) {
        if (mpaId == null) {
            return false;
        }
        String query = "SELECT COUNT(*) FROM mpa WHERE id = ?";
        Long count = jdbcTemplate.queryForObject(query, Long.class, mpaId);
        return count != null && count > 0;
    }

    public boolean genreExists(Long genreId) {
        if (genreId == null) {
            return false;
        }
        String query = "SELECT COUNT(*) FROM genre WHERE id = ?";
        Long count = jdbcTemplate.queryForObject(query, Long.class, genreId);
        return count != null && count > 0;
    }
}

package ru.yandex.practicum.filmorate.storage.genre;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.mapper.GenreRowMapper;

import java.util.List;

@Repository
public class GenreDbStorage implements GenreStorage {
    private final JdbcTemplate jdbcTemplate;

    public GenreDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Genre getGenreById(Long id) {
        String query = "SELECT * FROM genre WHERE id = ?;";
        try {
            return jdbcTemplate.queryForObject(query, new GenreRowMapper(), id);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public List<Genre> getAllGenres() {
        String query = "SELECT * FROM genre;";
        try {
            return jdbcTemplate.query(query, new GenreRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return List.of();
        }
    }
}
